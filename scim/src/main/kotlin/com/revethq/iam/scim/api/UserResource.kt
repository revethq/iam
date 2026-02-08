package com.revethq.iam.scim.api

import com.revethq.iam.scim.dtos.ScimListResponse
import com.revethq.iam.scim.dtos.ScimPatchOp
import com.revethq.iam.scim.dtos.ScimUser
import com.revethq.iam.scim.exception.ScimConflictException
import com.revethq.iam.scim.exception.ScimNotFoundException
import com.revethq.iam.scim.filter.ScimFilterHelper
import com.revethq.iam.scim.mappers.toDomain
import com.revethq.iam.scim.mappers.toScimUser
import com.revethq.iam.scim.mappers.updateDomain
import com.revethq.iam.user.persistence.service.UserService
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.UUID

@ScimEndpoint
@RegisterForReflection
@Path("/scim/v2/Users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SCIM Users", description = "SCIM 2.0 User management endpoints")
class UserResource {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var scimRequestContext: ScimRequestContext

    @Context
    lateinit var uriInfo: UriInfo

    private val baseUrl: String
        get() = uriInfo.baseUri.toString().removeSuffix("/")

    private val identityProviderId: UUID
        get() = scimRequestContext.identityProviderId
            ?: throw IllegalStateException("Identity provider ID not set in request context")

    @POST
    @Transactional
    @Operation(summary = "Create user", description = "Create a new SCIM user")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "User created successfully",
            content = [Content(schema = Schema(implementation = ScimUser::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "409", description = "User already exists")
    )
    fun createUser(
        @RequestBody(
            description = "User to create",
            required = true,
            content = [Content(schema = Schema(implementation = ScimUser::class))]
        )
        scimUser: ScimUser
    ): Response {
        // Check for existing user by username
        userService.findByUsername(scimUser.userName)?.let {
            throw ScimConflictException("User with username '${scimUser.userName}' already exists")
        }

        // Check for existing user by externalId
        scimUser.externalId?.let { extId ->
            userService.findByExternalId(extId, identityProviderId)?.let {
                throw ScimConflictException("User with externalId '$extId' already exists")
            }
        }

        val user = scimUser.toDomain()
        val created = userService.create(user, identityProviderId, scimUser.externalId)

        return Response.status(201)
            .entity(created.toScimUser(baseUrl, scimUser.externalId))
            .build()
    }

    @GET
    @Operation(summary = "List users", description = "List SCIM users with optional filtering and pagination")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of users"
        ),
        APIResponse(responseCode = "400", description = "Invalid filter syntax")
    )
    fun listUsers(
        @Parameter(description = "SCIM filter expression")
        @QueryParam("filter")
        filter: String?,

        @Parameter(description = "1-based start index for pagination")
        @QueryParam("startIndex")
        @DefaultValue("1")
        startIndex: Int,

        @Parameter(description = "Number of results per page")
        @QueryParam("count")
        @DefaultValue("100")
        count: Int
    ): ScimListResponse<ScimUser> {
        val result = ScimFilterHelper.filterUsers(
            filterString = filter,
            userService = userService,
            identityProviderId = identityProviderId,
            startIndex = startIndex - 1,
            count = count
        )

        val scimUsers = result.items.map { user ->
            val externalId = userService.getExternalId(user.id, identityProviderId)
            user.toScimUser(baseUrl, externalId)
        }

        return ScimListResponse(
            totalResults = result.totalCount.toInt(),
            startIndex = startIndex,
            itemsPerPage = scimUsers.size,
            resources = scimUsers
        )
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get user", description = "Get a SCIM user by ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User found",
            content = [Content(schema = Schema(implementation = ScimUser::class))]
        ),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun getUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id")
        id: String
    ): ScimUser {
        val uuid = UUID.fromString(id)
        val user = userService.findById(uuid)
            ?: throw ScimNotFoundException("User", id)
        val externalId = userService.getExternalId(uuid, identityProviderId)
        return user.toScimUser(baseUrl, externalId)
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Replace user", description = "Replace a SCIM user entirely")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User replaced successfully",
            content = [Content(schema = Schema(implementation = ScimUser::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun replaceUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id")
        id: String,

        @RequestBody(
            description = "User data",
            required = true,
            content = [Content(schema = Schema(implementation = ScimUser::class))]
        )
        scimUser: ScimUser
    ): ScimUser {
        val uuid = UUID.fromString(id)
        val existing = userService.findById(uuid)
            ?: throw ScimNotFoundException("User", id)

        val updated = scimUser.updateDomain(existing)
        val savedUser = userService.update(updated)
        userService.updateExternalId(uuid, identityProviderId, scimUser.externalId)

        return savedUser.toScimUser(baseUrl, scimUser.externalId)
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update user", description = "Partially update a SCIM user")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = [Content(schema = Schema(implementation = ScimUser::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid patch operation"),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun patchUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id")
        id: String,

        @RequestBody(
            description = "Patch operations",
            required = true,
            content = [Content(schema = Schema(implementation = ScimPatchOp::class))]
        )
        patchOp: ScimPatchOp
    ): ScimUser {
        val uuid = UUID.fromString(id)
        val existing = userService.findById(uuid)
            ?: throw ScimNotFoundException("User", id)

        var user = existing
        var newExternalId = userService.getExternalId(uuid, identityProviderId)

        for (operation in patchOp.operations) {
            when (operation.op.lowercase()) {
                "replace" -> {
                    when (operation.path?.lowercase()) {
                        "username" -> user = user.copy(username = operation.value.toString())
                        "active" -> {
                            val props = user.metadata.properties.orEmpty().toMutableMap()
                            props["active"] = operation.value as Boolean
                            user = user.copy(metadata = user.metadata.copy(properties = props))
                        }
                        "externalid" -> newExternalId = operation.value?.toString()
                        else -> {
                            // Handle nested paths or bulk replace
                            if (operation.path == null && operation.value is Map<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val values = operation.value as Map<String, Any?>
                                values["userName"]?.let { user = user.copy(username = it.toString()) }
                                values["externalId"]?.let { newExternalId = it.toString() }
                            }
                        }
                    }
                }
                "add" -> {
                    when (operation.path?.lowercase()) {
                        "externalid" -> newExternalId = operation.value?.toString()
                    }
                }
                "remove" -> {
                    when (operation.path?.lowercase()) {
                        "externalid" -> newExternalId = null
                    }
                }
            }
        }

        val savedUser = userService.update(user)
        userService.updateExternalId(uuid, identityProviderId, newExternalId)

        return savedUser.toScimUser(baseUrl, newExternalId)
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete user", description = "Delete a SCIM user")
    @APIResponses(
        APIResponse(responseCode = "204", description = "User deleted successfully"),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun deleteUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id")
        id: String
    ): Response {
        val uuid = UUID.fromString(id)
        if (!userService.delete(uuid)) {
            throw ScimNotFoundException("User", id)
        }
        return Response.noContent().build()
    }
}

package com.revethq.iam.scim.api

import com.revethq.iam.scim.dtos.ScimGroup
import com.revethq.iam.scim.dtos.ScimListResponse
import com.revethq.iam.scim.dtos.ScimPatchOp
import com.revethq.iam.scim.exception.ScimConflictException
import com.revethq.iam.scim.exception.ScimNotFoundException
import com.revethq.iam.scim.filter.ScimFilterHelper
import com.revethq.iam.scim.mappers.toDomain
import com.revethq.iam.scim.mappers.toGroupMember
import com.revethq.iam.scim.mappers.toScimGroup
import com.revethq.iam.scim.mappers.updateDomain
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.persistence.service.GroupService
import com.revethq.iam.user.persistence.service.UserService
import io.quarkus.runtime.annotations.RegisterForReflection
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
import java.util.UUID

@ScimEndpoint
@RegisterForReflection
@Path("/scim/v2/Groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SCIM Groups", description = "SCIM 2.0 Group management endpoints")
class GroupResource {
    @Inject
    lateinit var groupService: GroupService

    @Inject
    lateinit var userService: UserService

    @Context
    lateinit var uriInfo: UriInfo

    private val baseUrl: String
        get() = uriInfo.baseUri.toString().removeSuffix("/")

    @POST
    @Transactional
    @Operation(summary = "Create group", description = "Create a new SCIM group")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Group created successfully",
            content = [Content(schema = Schema(implementation = ScimGroup::class))],
        ),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "409", description = "Group already exists"),
    )
    fun createGroup(
        @RequestBody(
            description = "Group to create",
            required = true,
            content = [Content(schema = Schema(implementation = ScimGroup::class))],
        )
        scimGroup: ScimGroup,
    ): Response {
        // Check for existing group by displayName
        groupService.findByDisplayName(scimGroup.displayName)?.let {
            throw ScimConflictException("Group with displayName '${scimGroup.displayName}' already exists")
        }

        // Check for existing group by externalId
        scimGroup.externalId?.let { extId ->
            groupService.findByExternalId(extId)?.let {
                throw ScimConflictException("Group with externalId '$extId' already exists")
            }
        }

        val group = scimGroup.toDomain()
        val created = groupService.create(group)

        // Add members if provided
        val members =
            scimGroup.members?.map { member ->
                val groupMember = member.toGroupMember(created.id)
                groupService.addMember(created.id, groupMember)
            } ?: emptyList()

        val memberPairs =
            members.map { member ->
                member to userService.findById(member.memberId)
            }

        return Response
            .status(201)
            .entity(created.toScimGroup(baseUrl, memberPairs))
            .build()
    }

    @GET
    @Operation(summary = "List groups", description = "List SCIM groups with optional filtering and pagination")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of groups",
        ),
        APIResponse(responseCode = "400", description = "Invalid filter syntax"),
    )
    fun listGroups(
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
        count: Int,
    ): ScimListResponse<ScimGroup> {
        val result =
            ScimFilterHelper.filterGroups(
                filterString = filter,
                groupService = groupService,
                startIndex = startIndex - 1,
                count = count,
            )

        val scimGroups =
            result.items.map { group ->
                val members = groupService.getMembers(group.id)
                val memberPairs =
                    members.map { member ->
                        member to userService.findById(member.memberId)
                    }
                group.toScimGroup(baseUrl, memberPairs)
            }

        return ScimListResponse(
            totalResults = result.totalCount.toInt(),
            startIndex = startIndex,
            itemsPerPage = scimGroups.size,
            resources = scimGroups,
        )
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get group", description = "Get a SCIM group by ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Group found",
            content = [Content(schema = Schema(implementation = ScimGroup::class))],
        ),
        APIResponse(responseCode = "404", description = "Group not found"),
    )
    fun getGroup(
        @Parameter(description = "Group ID", required = true)
        @PathParam("id")
        id: String,
    ): ScimGroup {
        val uuid = UUID.fromString(id)
        val group =
            groupService.findById(uuid)
                ?: throw ScimNotFoundException("Group", id)

        val members = groupService.getMembers(uuid)
        val memberPairs =
            members.map { member ->
                member to userService.findById(member.memberId)
            }

        return group.toScimGroup(baseUrl, memberPairs)
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Replace group", description = "Replace a SCIM group entirely")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Group replaced successfully",
            content = [Content(schema = Schema(implementation = ScimGroup::class))],
        ),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "404", description = "Group not found"),
    )
    fun replaceGroup(
        @Parameter(description = "Group ID", required = true)
        @PathParam("id")
        id: String,
        @RequestBody(
            description = "Group data",
            required = true,
            content = [Content(schema = Schema(implementation = ScimGroup::class))],
        )
        scimGroup: ScimGroup,
    ): ScimGroup {
        val uuid = UUID.fromString(id)
        val existing =
            groupService.findById(uuid)
                ?: throw ScimNotFoundException("Group", id)

        val updated = scimGroup.updateDomain(existing)
        val savedGroup = groupService.update(updated)

        // Replace members
        val newMembers = scimGroup.members?.map { it.toGroupMember(uuid) } ?: emptyList()
        val members = groupService.setMembers(uuid, newMembers)

        val memberPairs =
            members.map { member ->
                member to userService.findById(member.memberId)
            }

        return savedGroup.toScimGroup(baseUrl, memberPairs)
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update group", description = "Partially update a SCIM group (add/remove members)")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Group updated successfully",
            content = [Content(schema = Schema(implementation = ScimGroup::class))],
        ),
        APIResponse(responseCode = "400", description = "Invalid patch operation"),
        APIResponse(responseCode = "404", description = "Group not found"),
    )
    fun patchGroup(
        @Parameter(description = "Group ID", required = true)
        @PathParam("id")
        id: String,
        @RequestBody(
            description = "Patch operations",
            required = true,
            content = [Content(schema = Schema(implementation = ScimPatchOp::class))],
        )
        patchOp: ScimPatchOp,
    ): ScimGroup {
        val uuid = UUID.fromString(id)
        var group =
            groupService.findById(uuid)
                ?: throw ScimNotFoundException("Group", id)

        for (operation in patchOp.operations) {
            when (operation.op.lowercase()) {
                "replace" -> {
                    when (operation.path?.lowercase()) {
                        "displayname" -> {
                            group = group.copy(displayName = operation.value.toString())
                            groupService.update(group)
                        }
                        "externalid" -> {
                            group = group.copy(externalId = operation.value?.toString())
                            groupService.update(group)
                        }
                        "members" -> {
                            @Suppress("UNCHECKED_CAST")
                            val memberList = operation.value as? List<Map<String, Any?>> ?: emptyList()
                            val newMembers =
                                memberList.map { memberMap ->
                                    GroupMember(
                                        groupId = uuid,
                                        memberId = UUID.fromString(memberMap["value"].toString()),
                                    )
                                }
                            groupService.setMembers(uuid, newMembers)
                        }
                    }
                }
                "add" -> {
                    when (operation.path?.lowercase()) {
                        "members" -> {
                            @Suppress("UNCHECKED_CAST")
                            val memberList = operation.value as? List<Map<String, Any?>> ?: emptyList()
                            for (memberMap in memberList) {
                                val member =
                                    GroupMember(
                                        groupId = uuid,
                                        memberId = UUID.fromString(memberMap["value"].toString()),
                                    )
                                groupService.addMember(uuid, member)
                            }
                        }
                    }
                }
                "remove" -> {
                    val path = operation.path ?: continue
                    // Handle path like "members[value eq \"userId\"]"
                    val memberMatch = Regex("""members\[value eq "(.+)"]""").find(path)
                    if (memberMatch != null) {
                        val memberId = UUID.fromString(memberMatch.groupValues[1])
                        groupService.removeMember(uuid, memberId)
                    }
                }
            }
        }

        // Refresh group and get updated members
        group = groupService.findById(uuid)!!
        val members = groupService.getMembers(uuid)
        val memberPairs =
            members.map { member ->
                member to userService.findById(member.memberId)
            }

        return group.toScimGroup(baseUrl, memberPairs)
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete group", description = "Delete a SCIM group")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Group deleted successfully"),
        APIResponse(responseCode = "404", description = "Group not found"),
    )
    fun deleteGroup(
        @Parameter(description = "Group ID", required = true)
        @PathParam("id")
        id: String,
    ): Response {
        val uuid = UUID.fromString(id)
        if (!groupService.delete(uuid)) {
            throw ScimNotFoundException("Group", id)
        }
        return Response.noContent().build()
    }
}

package com.revethq.iam.user.web.api

import com.revethq.iam.user.persistence.service.UserService
import com.revethq.iam.user.web.dto.CreateUserRequest
import com.revethq.iam.user.web.dto.PageResponse
import com.revethq.iam.user.web.dto.UpdateUserRequest
import com.revethq.iam.user.web.dto.UserResponse
import com.revethq.iam.user.web.exception.UserConflictException
import com.revethq.iam.user.web.exception.UserNotFoundException
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserResource {

    @Inject
    lateinit var userService: UserService

    @POST
    fun createUser(
        request: CreateUserRequest,
        @HeaderParam("X-Identity-Provider-Id") identityProviderId: UUID?
    ): Response {
        userService.findByUsername(request.username)?.let {
            throw UserConflictException("User with username '${request.username}' already exists")
        }

        userService.findByEmail(request.email)?.let {
            throw UserConflictException("User with email '${request.email}' already exists")
        }

        val user = request.toDomain()
        val created = userService.create(
            user,
            identityProviderId ?: DEFAULT_IDENTITY_PROVIDER_ID,
            null
        )

        return Response.status(Response.Status.CREATED)
            .entity(UserResponse.fromDomain(created))
            .build()
    }

    @GET
    @Path("/{id}")
    fun getUser(@PathParam("id") id: String): UserResponse {
        val uuid = UUID.fromString(id)
        val user = userService.findById(uuid)
            ?: throw UserNotFoundException(id)
        return UserResponse.fromDomain(user)
    }

    @GET
    fun listUsers(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int
    ): PageResponse<UserResponse> {
        val result = userService.list(page * size, size + 1)
        val hasMore = result.items.size > size
        val content = result.items.take(size)

        return PageResponse(
            content = content.map { UserResponse.fromDomain(it) },
            page = page,
            size = size,
            hasMore = hasMore
        )
    }

    @PUT
    @Path("/{id}")
    fun updateUser(
        @PathParam("id") id: String,
        request: UpdateUserRequest
    ): UserResponse {
        val uuid = UUID.fromString(id)
        val existing = userService.findById(uuid)
            ?: throw UserNotFoundException(id)

        val updated = existing.copy(
            username = request.username,
            email = request.email
        )

        val saved = userService.update(updated)
        return UserResponse.fromDomain(saved)
    }

    @DELETE
    @Path("/{id}")
    fun deleteUser(@PathParam("id") id: String): Response {
        val uuid = UUID.fromString(id)
        if (!userService.delete(uuid)) {
            throw UserNotFoundException(id)
        }
        return Response.noContent().build()
    }

    companion object {
        // Default identity provider ID for direct API access (not via SCIM)
        private val DEFAULT_IDENTITY_PROVIDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}

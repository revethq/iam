package com.revethq.iam.user.web.api

import com.revethq.iam.user.persistence.service.GroupService
import com.revethq.iam.user.web.dto.AddMemberRequest
import com.revethq.iam.user.web.dto.CreateGroupRequest
import com.revethq.iam.user.web.dto.GroupMemberResponse
import com.revethq.iam.user.web.dto.GroupResponse
import com.revethq.iam.user.web.dto.PageResponse
import com.revethq.iam.user.web.dto.UpdateGroupRequest
import com.revethq.iam.user.web.exception.GroupConflictException
import com.revethq.iam.user.web.exception.GroupNotFoundException
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GroupResource {
    @Inject
    lateinit var groupService: GroupService

    @POST
    fun createGroup(request: CreateGroupRequest): Response {
        groupService.findByDisplayName(request.displayName)?.let {
            throw GroupConflictException("Group with displayName '${request.displayName}' already exists")
        }

        val group = request.toDomain()
        val created = groupService.create(group)

        return Response
            .status(Response.Status.CREATED)
            .entity(GroupResponse.fromDomain(created))
            .build()
    }

    @GET
    @Path("/{id}")
    fun getGroup(
        @PathParam("id") id: String,
    ): GroupResponse {
        val uuid = UUID.fromString(id)
        val group =
            groupService.findById(uuid)
                ?: throw GroupNotFoundException(id)
        return GroupResponse.fromDomain(group)
    }

    @GET
    fun listGroups(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int,
    ): PageResponse<GroupResponse> {
        val result = groupService.list(page * size, size + 1)
        val hasMore = result.items.size > size
        val content = result.items.take(size)

        return PageResponse(
            content = content.map { GroupResponse.fromDomain(it) },
            page = page,
            size = size,
            hasMore = hasMore,
        )
    }

    @PUT
    @Path("/{id}")
    fun updateGroup(
        @PathParam("id") id: String,
        request: UpdateGroupRequest,
    ): GroupResponse {
        val uuid = UUID.fromString(id)
        val existing =
            groupService.findById(uuid)
                ?: throw GroupNotFoundException(id)

        val updated =
            existing.copy(
                displayName = request.displayName,
            )

        val saved = groupService.update(updated)
        return GroupResponse.fromDomain(saved)
    }

    @DELETE
    @Path("/{id}")
    fun deleteGroup(
        @PathParam("id") id: String,
    ): Response {
        val uuid = UUID.fromString(id)
        if (!groupService.delete(uuid)) {
            throw GroupNotFoundException(id)
        }
        return Response.noContent().build()
    }

    // Member management endpoints

    @GET
    @Path("/{id}/members")
    fun getMembers(
        @PathParam("id") id: String,
    ): List<GroupMemberResponse> {
        val uuid = UUID.fromString(id)
        groupService.findById(uuid) ?: throw GroupNotFoundException(id)

        return groupService.getMembers(uuid).map { GroupMemberResponse.fromDomain(it) }
    }

    @POST
    @Path("/{id}/members")
    fun addMember(
        @PathParam("id") id: String,
        request: AddMemberRequest,
    ): Response {
        val uuid = UUID.fromString(id)
        groupService.findById(uuid) ?: throw GroupNotFoundException(id)

        val member = request.toDomain(uuid)
        val created = groupService.addMember(uuid, member)

        return Response
            .status(Response.Status.CREATED)
            .entity(GroupMemberResponse.fromDomain(created))
            .build()
    }

    @DELETE
    @Path("/{id}/members/{memberId}")
    fun removeMember(
        @PathParam("id") id: String,
        @PathParam("memberId") memberId: String,
    ): Response {
        val groupUuid = UUID.fromString(id)
        val memberUuid = UUID.fromString(memberId)

        groupService.findById(groupUuid) ?: throw GroupNotFoundException(id)
        groupService.removeMember(groupUuid, memberUuid)

        return Response.noContent().build()
    }
}

package com.revethq.iam.permission.web.api

import com.revethq.iam.permission.persistence.service.PolicyAttachmentService
import com.revethq.iam.permission.web.dto.AttachedPolicyResponse
import com.revethq.iam.permission.web.dto.PageResponse
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GroupPolicyResource {

    @Inject
    lateinit var policyAttachmentService: PolicyAttachmentService

    @GET
    @Path("/{id}/policies")
    fun listPoliciesForGroup(
        @PathParam("id") id: String,
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int
    ): PageResponse<AttachedPolicyResponse> {
        val principalUrn = "urn:revet:iam::group/$id"
        val allPolicies = policyAttachmentService.listAttachedPoliciesForPrincipal(principalUrn)

        val start = page * size
        val end = minOf(start + size + 1, allPolicies.size)
        val pageContent = if (start < allPolicies.size) {
            allPolicies.subList(start, minOf(start + size, allPolicies.size))
        } else {
            emptyList()
        }
        val hasMore = end > start + size

        return PageResponse(
            content = pageContent.map { AttachedPolicyResponse.fromDomain(it) },
            page = page,
            size = size,
            hasMore = hasMore
        )
    }
}

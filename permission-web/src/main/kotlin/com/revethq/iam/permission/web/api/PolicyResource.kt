package com.revethq.iam.permission.web.api

import com.revethq.iam.permission.persistence.service.PolicyAttachmentService
import com.revethq.iam.permission.persistence.service.PolicyService
import com.revethq.iam.permission.web.dto.AttachPolicyRequest
import com.revethq.iam.permission.web.dto.CreatePolicyRequest
import com.revethq.iam.permission.web.dto.PolicyAttachmentListResponse
import com.revethq.iam.permission.web.dto.PolicyAttachmentResponse
import com.revethq.iam.permission.web.dto.PolicyListResponse
import com.revethq.iam.permission.web.dto.PolicyResponse
import com.revethq.iam.permission.web.dto.UpdatePolicyRequest
import com.revethq.iam.permission.web.exception.PolicyAttachmentConflictException
import com.revethq.iam.permission.web.exception.PolicyAttachmentNotFoundException
import com.revethq.iam.permission.web.exception.PolicyConflictException
import com.revethq.iam.permission.web.exception.PolicyNotFoundException
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

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PolicyResource {

    @Inject
    lateinit var policyService: PolicyService

    @Inject
    lateinit var policyAttachmentService: PolicyAttachmentService

    @POST
    fun createPolicy(request: CreatePolicyRequest): Response {
        policyService.findByName(request.name, request.tenantId)?.let {
            throw PolicyConflictException("Policy with name '${request.name}' already exists")
        }

        val policy = request.toDomain()
        val created = policyService.create(policy)

        return Response.status(Response.Status.CREATED)
            .entity(PolicyResponse.fromDomain(created))
            .build()
    }

    @GET
    @Path("/{id}")
    fun getPolicy(@PathParam("id") id: String): PolicyResponse {
        val uuid = UUID.fromString(id)
        val policy = policyService.findById(uuid)
            ?: throw PolicyNotFoundException(id)
        return PolicyResponse.fromDomain(policy)
    }

    @GET
    fun listPolicies(
        @QueryParam("startIndex") @DefaultValue("0") startIndex: Int,
        @QueryParam("count") @DefaultValue("100") count: Int,
        @QueryParam("tenantId") tenantId: String?
    ): PolicyListResponse {
        val result = policyService.list(startIndex, count, tenantId)
        return PolicyListResponse(
            items = result.items.map { PolicyResponse.fromDomain(it) },
            totalCount = result.totalCount,
            startIndex = result.startIndex,
            itemsPerPage = result.itemsPerPage
        )
    }

    @PUT
    @Path("/{id}")
    fun updatePolicy(
        @PathParam("id") id: String,
        request: UpdatePolicyRequest
    ): PolicyResponse {
        val uuid = UUID.fromString(id)
        val existing = policyService.findById(uuid)
            ?: throw PolicyNotFoundException(id)

        val updated = existing.copy(
            name = request.name,
            description = request.description,
            version = request.version,
            statements = request.statements.map { it.toDomain() },
            tenantId = request.tenantId
        )

        val saved = policyService.update(updated)
        return PolicyResponse.fromDomain(saved)
    }

    @DELETE
    @Path("/{id}")
    fun deletePolicy(@PathParam("id") id: String): Response {
        val uuid = UUID.fromString(id)
        if (!policyService.delete(uuid)) {
            throw PolicyNotFoundException(id)
        }
        return Response.noContent().build()
    }

    @POST
    @Path("/{id}/attachments")
    fun attachPolicy(
        @PathParam("id") id: String,
        request: AttachPolicyRequest
    ): Response {
        val uuid = UUID.fromString(id)
        policyService.findById(uuid) ?: throw PolicyNotFoundException(id)

        try {
            val attachment = policyAttachmentService.attach(uuid, request.principalUrn)
            return Response.status(Response.Status.CREATED)
                .entity(PolicyAttachmentResponse.fromDomain(attachment))
                .build()
        } catch (e: IllegalStateException) {
            throw PolicyAttachmentConflictException(id, request.principalUrn)
        }
    }

    @DELETE
    @Path("/{id}/attachments/{attachmentId}")
    fun detachPolicy(
        @PathParam("id") id: String,
        @PathParam("attachmentId") attachmentId: String
    ): Response {
        val policyUuid = UUID.fromString(id)
        val attachmentUuid = UUID.fromString(attachmentId)
        if (!policyAttachmentService.detach(policyUuid, attachmentUuid)) {
            throw PolicyAttachmentNotFoundException(attachmentId)
        }
        return Response.noContent().build()
    }

    @GET
    @Path("/{id}/attachments")
    fun listAttachments(@PathParam("id") id: String): PolicyAttachmentListResponse {
        val uuid = UUID.fromString(id)
        policyService.findById(uuid) ?: throw PolicyNotFoundException(id)

        val attachments = policyAttachmentService.listAttachmentsForPolicy(uuid)
        return PolicyAttachmentListResponse(
            items = attachments.map { PolicyAttachmentResponse.fromDomain(it) }
        )
    }
}

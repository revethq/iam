package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.PolicyAttachment
import java.util.UUID

/**
 * Represents a policy along with its attachment information.
 */
data class AttachedPolicy(
    val attachmentId: UUID,
    val policy: Policy,
    val attachedOn: java.time.OffsetDateTime?,
    val attachedBy: String?
)

interface PolicyAttachmentService {
    fun attach(policyId: UUID, principalUrn: String, attachedBy: String? = null): PolicyAttachment
    fun findById(attachmentId: UUID): PolicyAttachment?
    fun detach(policyId: UUID, attachmentId: UUID): Boolean
    fun listAttachmentsForPolicy(policyId: UUID): List<PolicyAttachment>
    fun listPoliciesForPrincipal(principalUrn: String): List<Policy>
    fun listAttachedPoliciesForPrincipal(principalUrn: String): List<AttachedPolicy>
    fun isAttached(policyId: UUID, principalUrn: String): Boolean
}

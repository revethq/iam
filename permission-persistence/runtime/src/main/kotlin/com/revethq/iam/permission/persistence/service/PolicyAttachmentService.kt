package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.PolicyAttachment
import java.util.UUID

interface PolicyAttachmentService {
    fun attach(policyId: UUID, principalUrn: String, attachedBy: String? = null): PolicyAttachment
    fun detach(policyId: UUID, principalUrn: String): Boolean
    fun listAttachmentsForPolicy(policyId: UUID): List<PolicyAttachment>
    fun listPoliciesForPrincipal(principalUrn: String): List<Policy>
    fun isAttached(policyId: UUID, principalUrn: String): Boolean
}

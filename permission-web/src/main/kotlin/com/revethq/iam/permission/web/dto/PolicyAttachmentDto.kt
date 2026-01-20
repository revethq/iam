package com.revethq.iam.permission.web.dto

import com.revethq.iam.permission.domain.PolicyAttachment
import java.time.OffsetDateTime
import java.util.UUID

data class AttachPolicyRequest(
    val principalUrn: String
)

data class PolicyAttachmentResponse(
    val id: UUID,
    val policyId: UUID,
    val principalUrn: String,
    val attachedOn: OffsetDateTime?,
    val attachedBy: String?
) {
    companion object {
        fun fromDomain(attachment: PolicyAttachment): PolicyAttachmentResponse = PolicyAttachmentResponse(
            id = attachment.id,
            policyId = attachment.policyId,
            principalUrn = attachment.principalUrn,
            attachedOn = attachment.attachedOn,
            attachedBy = attachment.attachedBy
        )
    }
}

data class PolicyAttachmentListResponse(
    val items: List<PolicyAttachmentResponse>
)

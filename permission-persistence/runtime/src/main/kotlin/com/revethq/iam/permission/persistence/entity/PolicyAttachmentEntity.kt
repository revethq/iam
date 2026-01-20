package com.revethq.iam.permission.persistence.entity

import com.revethq.iam.permission.domain.PolicyAttachment
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_policy_attachments")
class PolicyAttachmentEntity {

    @Id
    lateinit var id: UUID

    @Column(name = "policy_id", nullable = false)
    lateinit var policyId: UUID

    @Column(name = "principal_urn", nullable = false)
    lateinit var principalUrn: String

    @Column(name = "attached_on", nullable = false)
    lateinit var attachedOn: OffsetDateTime

    @Column(name = "attached_by")
    var attachedBy: String? = null

    fun toDomain(): PolicyAttachment = PolicyAttachment(
        id = id,
        policyId = policyId,
        principalUrn = principalUrn,
        attachedOn = attachedOn,
        attachedBy = attachedBy
    )

    companion object {
        fun fromDomain(attachment: PolicyAttachment): PolicyAttachmentEntity = PolicyAttachmentEntity().apply {
            val now = OffsetDateTime.now()
            id = attachment.id
            policyId = attachment.policyId
            principalUrn = attachment.principalUrn
            attachedOn = attachment.attachedOn ?: now
            attachedBy = attachment.attachedBy
        }
    }
}

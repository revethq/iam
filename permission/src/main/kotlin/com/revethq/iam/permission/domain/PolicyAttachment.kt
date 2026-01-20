package com.revethq.iam.permission.domain

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Represents the attachment of a policy to a principal (user or group).
 *
 * @property id Unique identifier for the attachment
 * @property policyId ID of the attached policy
 * @property principalUrn URN of the principal (user or group) the policy is attached to
 * @property attachedOn When the policy was attached
 * @property attachedBy URN of the principal who attached the policy (optional)
 */
data class PolicyAttachment(
    var id: UUID,
    var policyId: UUID,
    var principalUrn: String,
    var attachedOn: OffsetDateTime? = null,
    var attachedBy: String? = null
)

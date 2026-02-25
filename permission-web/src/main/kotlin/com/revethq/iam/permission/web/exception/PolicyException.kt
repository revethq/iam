package com.revethq.iam.permission.web.exception

open class PolicyException(
    message: String,
    val statusCode: Int,
) : RuntimeException(message)

class PolicyNotFoundException(
    id: String,
) : PolicyException("Policy not found: $id", 404)

class PolicyConflictException(
    message: String,
) : PolicyException(message, 409)

class PolicyAttachmentConflictException(
    policyId: String,
    principalUrn: String,
) : PolicyException("Policy $policyId is already attached to $principalUrn", 409)

class PolicyAttachmentNotFoundException(
    attachmentId: String,
) : PolicyException("Policy attachment not found: $attachmentId", 404)

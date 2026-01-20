package com.revethq.iam.permission.evaluation

import com.revethq.iam.permission.condition.ConditionContext

/**
 * Represents an authorization request to be evaluated against policies.
 *
 * @property principalUrn URN of the principal making the request
 * @property action The action being requested (e.g., "iam:CreateUser")
 * @property resourceUrn URN of the resource being accessed
 * @property context Additional context for condition evaluation
 */
data class AuthorizationRequest(
    val principalUrn: String,
    val action: String,
    val resourceUrn: String,
    val context: ConditionContext = ConditionContext()
) {
    /**
     * Create a ConditionContext populated with this request's data.
     */
    fun toConditionContext(): ConditionContext = context.copy(
        principalId = principalUrn,
        requestedAction = action,
        requestedResource = resourceUrn
    )
}

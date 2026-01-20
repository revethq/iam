package com.revethq.iam.permission.evaluation

import com.revethq.iam.permission.domain.Statement

/**
 * The decision from policy evaluation.
 */
enum class AuthorizationDecision {
    ALLOW,
    DENY
}

/**
 * Result of evaluating an authorization request against policies.
 *
 * @property decision The final authorization decision (ALLOW or DENY)
 * @property matchingStatements List of statements that matched the request
 * @property isExplicitDeny True if the deny was from an explicit Deny statement
 */
data class AuthorizationResult(
    val decision: AuthorizationDecision,
    val matchingStatements: List<MatchedStatement> = emptyList(),
    val isExplicitDeny: Boolean = false
) {
    /**
     * Check if access is allowed.
     */
    fun isAllowed(): Boolean = decision == AuthorizationDecision.ALLOW

    /**
     * Check if access is denied.
     */
    fun isDenied(): Boolean = decision == AuthorizationDecision.DENY

    companion object {
        /**
         * Create an implicit deny result (no matching statements).
         */
        fun implicitDeny(): AuthorizationResult = AuthorizationResult(
            decision = AuthorizationDecision.DENY,
            matchingStatements = emptyList(),
            isExplicitDeny = false
        )

        /**
         * Create an explicit deny result.
         */
        fun explicitDeny(matchingStatements: List<MatchedStatement>): AuthorizationResult = AuthorizationResult(
            decision = AuthorizationDecision.DENY,
            matchingStatements = matchingStatements,
            isExplicitDeny = true
        )

        /**
         * Create an allow result.
         */
        fun allow(matchingStatements: List<MatchedStatement>): AuthorizationResult = AuthorizationResult(
            decision = AuthorizationDecision.ALLOW,
            matchingStatements = matchingStatements,
            isExplicitDeny = false
        )
    }
}

/**
 * A statement that matched during policy evaluation.
 *
 * @property statement The matching statement
 * @property policyName Name of the policy containing the statement
 */
data class MatchedStatement(
    val statement: Statement,
    val policyName: String
)

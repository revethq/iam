package com.revethq.iam.permission.domain

/**
 * Utility for matching actions against action patterns.
 *
 * Action format: {service}:{action} or {service}:* for wildcard
 * Examples:
 * - iam:CreateUser
 * - iam:*
 * - *
 */
object ActionMatcher {
    /**
     * Check if an action matches a pattern.
     *
     * @param action The action being requested (e.g., "iam:CreateUser")
     * @param pattern The pattern from a policy statement (e.g., "iam:*")
     * @return true if the action matches the pattern
     */
    fun matches(
        action: String,
        pattern: String,
    ): Boolean {
        // Full wildcard matches everything
        if (pattern == "*") return true

        val actionParts = action.split(":", limit = 2)
        val patternParts = pattern.split(":", limit = 2)

        // Invalid format
        if (actionParts.size != 2 || patternParts.size != 2) {
            return action == pattern
        }

        val (actionService, actionName) = actionParts
        val (patternService, patternName) = patternParts

        // Service must match (no wildcards in service part)
        if (actionService != patternService) return false

        // Action name wildcard
        if (patternName == "*") return true

        // Exact match
        return actionName == patternName
    }

    /**
     * Check if an action matches any of the given patterns.
     */
    fun matchesAny(
        action: String,
        patterns: List<String>,
    ): Boolean = patterns.any { matches(action, it) }
}

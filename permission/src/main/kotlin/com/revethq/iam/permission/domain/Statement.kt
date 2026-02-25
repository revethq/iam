package com.revethq.iam.permission.domain

/**
 * A statement within a policy that defines permissions.
 *
 * @property sid Optional statement identifier for debugging/logging
 * @property effect Whether this statement allows or denies access
 * @property actions List of actions (supports wildcards like "iam:*")
 * @property resources List of resource URNs (supports wildcards)
 * @property conditions Optional conditions that must be true for the statement to apply
 */
data class Statement(
    val sid: String? = null,
    val effect: Effect,
    val actions: List<String>,
    val resources: List<String>,
    val conditions: Map<String, Map<String, List<String>>> = emptyMap(),
) {
    init {
        require(actions.isNotEmpty()) { "Statement must have at least one action" }
        require(resources.isNotEmpty()) { "Statement must have at least one resource" }
    }

    /**
     * Check if this statement's actions include the requested action.
     */
    fun matchesAction(action: String): Boolean = ActionMatcher.matchesAny(action, actions)

    /**
     * Check if this statement's resources include the requested resource.
     */
    fun matchesResource(resource: String): Boolean {
        val resourceUrn = Urn.parse(resource) ?: return false
        return resources.any { pattern ->
            val patternUrn = Urn.parse(pattern) ?: return@any false
            resourceUrn.matches(patternUrn)
        }
    }

    /**
     * Check if this statement's resources include the requested resource URN.
     */
    fun matchesResource(resource: Urn): Boolean {
        return resources.any { pattern ->
            val patternUrn = Urn.parse(pattern) ?: return@any false
            resource.matches(patternUrn)
        }
    }
}

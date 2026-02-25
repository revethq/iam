package com.revethq.iam.permission.domain

/**
 * Uniform Resource Name (URN) for globally unique identification of principals and resources.
 *
 * Format: urn:{namespace}:{service}:{tenant}:{resource-type}/{resource-id}
 *
 * Examples:
 * - urn:revet:iam::user/alice (global user)
 * - urn:revet:iam:acme-corp:group/admins (tenant-scoped group)
 * - urn:revet:storage:acme-corp:bucket/reports (storage bucket)
 */
data class Urn(
    val namespace: String,
    val service: String,
    val tenant: String,
    val resourceType: String,
    val resourceId: String,
) {
    override fun toString(): String = "urn:$namespace:$service:$tenant:$resourceType/$resourceId"

    /**
     * Check if this URN matches a pattern URN.
     * Supports wildcards:
     * - `*` matches any single path segment
     * - `**` matches zero or more path segments (for hierarchical resources)
     */
    fun matches(pattern: Urn): Boolean {
        if (!matchesComponent(namespace, pattern.namespace)) return false
        if (!matchesComponent(service, pattern.service)) return false
        if (!matchesComponent(tenant, pattern.tenant)) return false
        if (!matchesComponent(resourceType, pattern.resourceType)) return false
        return matchesResourceId(resourceId, pattern.resourceId)
    }

    /**
     * Check if this URN matches a pattern string.
     */
    fun matches(pattern: String): Boolean {
        val parsed = parse(pattern) ?: return false
        return matches(parsed)
    }

    private fun matchesComponent(
        value: String,
        pattern: String,
    ): Boolean {
        if (pattern == "*") return true
        return value == pattern
    }

    private fun matchesResourceId(
        value: String,
        pattern: String,
    ): Boolean {
        // ** matches anything (zero or more segments)
        if (pattern == "**") return true

        // * matches exactly one segment (no slashes in value)
        if (pattern == "*") return !value.contains("/")

        val valueSegments = value.split("/")
        val patternSegments = pattern.split("/")

        return matchSegments(valueSegments, patternSegments, 0, 0)
    }

    private fun matchSegments(
        valueSegments: List<String>,
        patternSegments: List<String>,
        valueIndex: Int,
        patternIndex: Int,
    ): Boolean {
        // Base case: both exhausted
        if (patternIndex >= patternSegments.size && valueIndex >= valueSegments.size) {
            return true
        }

        // Pattern exhausted but value has more segments
        if (patternIndex >= patternSegments.size) {
            return false
        }

        val currentPattern = patternSegments[patternIndex]

        // ** matches zero or more segments
        if (currentPattern == "**") {
            // Try matching zero segments (skip **)
            if (matchSegments(valueSegments, patternSegments, valueIndex, patternIndex + 1)) {
                return true
            }
            // Try matching one or more segments
            if (valueIndex < valueSegments.size) {
                return matchSegments(valueSegments, patternSegments, valueIndex + 1, patternIndex)
            }
            return false
        }

        // Value exhausted but pattern has more (non-**) segments
        if (valueIndex >= valueSegments.size) {
            return false
        }

        val currentValue = valueSegments[valueIndex]

        // * matches exactly one segment
        if (currentPattern == "*") {
            return matchSegments(valueSegments, patternSegments, valueIndex + 1, patternIndex + 1)
        }

        // Exact match
        if (currentValue == currentPattern) {
            return matchSegments(valueSegments, patternSegments, valueIndex + 1, patternIndex + 1)
        }

        return false
    }

    companion object {
        private val URN_PATTERN = Regex("^urn:([^:]+):([^:]+):([^:]*):([^/]+)/(.+)$")

        /**
         * Parse a URN string into a Urn object.
         * Returns null if the string is not a valid URN.
         */
        fun parse(urn: String): Urn? {
            val match = URN_PATTERN.matchEntire(urn) ?: return null
            return Urn(
                namespace = match.groupValues[1],
                service = match.groupValues[2],
                tenant = match.groupValues[3],
                resourceType = match.groupValues[4],
                resourceId = match.groupValues[5],
            )
        }

        /**
         * Parse a URN string into a Urn object.
         * Throws IllegalArgumentException if the string is not a valid URN.
         */
        fun parseOrThrow(urn: String): Urn =
            parse(urn)
                ?: throw IllegalArgumentException("Invalid URN format: $urn")
    }
}

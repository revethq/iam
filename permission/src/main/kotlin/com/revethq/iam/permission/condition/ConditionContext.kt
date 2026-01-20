package com.revethq.iam.permission.condition

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Context for condition evaluation, providing values for context variables.
 *
 * @property principalId URN of the calling principal
 * @property sourceIp Request source IP address
 * @property requestedAction Action being requested
 * @property requestedResource Resource URN being accessed
 * @property currentTime Current time (defaults to now)
 * @property customVariables Additional custom variables for evaluation
 */
data class ConditionContext(
    val principalId: String? = null,
    val sourceIp: String? = null,
    val requestedAction: String? = null,
    val requestedResource: String? = null,
    val currentTime: OffsetDateTime = OffsetDateTime.now(),
    val customVariables: Map<String, String> = emptyMap()
) {
    /**
     * Resolve a variable reference like ${revet:PrincipalId} to its actual value.
     * Returns empty string if the variable is not found.
     */
    fun resolveVariable(variable: String): String {
        // Check if it's a variable reference
        if (!variable.startsWith("\${") || !variable.endsWith("}")) {
            return variable
        }

        val varName = variable.substring(2, variable.length - 1)
        return when (varName) {
            "revet:PrincipalId" -> principalId ?: ""
            "revet:SourceIp" -> sourceIp ?: ""
            "revet:RequestedAction" -> requestedAction ?: ""
            "revet:RequestedResource" -> requestedResource ?: ""
            "revet:CurrentTime" -> currentTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            else -> customVariables[varName] ?: ""
        }
    }

    /**
     * Resolve variables in a string, replacing all ${...} references.
     */
    fun resolveVariables(value: String): String {
        val regex = Regex("\\$\\{[^}]+\\}")
        return regex.replace(value) { match ->
            resolveVariable(match.value)
        }
    }

    /**
     * Get a value from the context by key name.
     * Used for condition evaluation when checking context keys.
     */
    fun getValue(key: String): String? {
        return when (key) {
            "revet:PrincipalId" -> principalId
            "revet:SourceIp" -> sourceIp
            "revet:RequestedAction" -> requestedAction
            "revet:RequestedResource" -> requestedResource
            "revet:CurrentTime" -> currentTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            else -> customVariables[key]
        }
    }

    /**
     * Check if a key exists in the context (has a non-null value).
     */
    fun hasKey(key: String): Boolean = getValue(key) != null
}

package com.revethq.iam.permission.condition

import java.math.BigDecimal
import java.net.InetAddress
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Evaluates policy conditions against a context.
 */
object ConditionEvaluator {
    /**
     * Evaluate all conditions in a statement.
     * All conditions must pass (AND logic).
     * Multiple values for the same key use OR logic.
     *
     * @param conditions Map of operator -> (key -> values)
     * @param context The evaluation context
     * @return true if all conditions pass, false otherwise
     */
    fun evaluate(
        conditions: Map<String, Map<String, List<String>>>,
        context: ConditionContext,
    ): Boolean {
        // All operator blocks must pass (AND)
        for ((operatorName, keyValueMap) in conditions) {
            val operator =
                ConditionOperator.fromName(operatorName)
                    ?: return false // Unknown operator fails evaluation

            // All keys must pass (AND)
            for ((key, values) in keyValueMap) {
                val contextValue = context.getValue(key)

                // Resolve any variables in the condition values
                val resolvedValues = values.map { context.resolveVariables(it) }

                if (!evaluateCondition(operator, contextValue, resolvedValues, context)) {
                    return false
                }
            }
        }
        return true
    }

    private fun evaluateCondition(
        operator: ConditionOperator,
        contextValue: String?,
        conditionValues: List<String>,
        context: ConditionContext,
    ): Boolean =
        when (operator) {
            // String conditions
            ConditionOperator.STRING_EQUALS -> {
                contextValue != null && conditionValues.any { it == contextValue }
            }

            ConditionOperator.STRING_NOT_EQUALS -> {
                contextValue != null && conditionValues.none { it == contextValue }
            }

            ConditionOperator.STRING_EQUALS_IGNORE_CASE -> {
                contextValue != null && conditionValues.any { it.equals(contextValue, ignoreCase = true) }
            }

            ConditionOperator.STRING_NOT_EQUALS_IGNORE_CASE -> {
                contextValue != null && conditionValues.none { it.equals(contextValue, ignoreCase = true) }
            }

            ConditionOperator.STRING_LIKE -> {
                contextValue != null && conditionValues.any { matchesLike(contextValue, it) }
            }

            ConditionOperator.STRING_NOT_LIKE -> {
                contextValue != null && conditionValues.none { matchesLike(contextValue, it) }
            }

            // Numeric conditions
            ConditionOperator.NUMERIC_EQUALS -> {
                evaluateNumeric(contextValue, conditionValues) { cv, v -> cv.compareTo(v) == 0 }
            }

            ConditionOperator.NUMERIC_NOT_EQUALS -> {
                evaluateNumericNone(contextValue, conditionValues) { cv, v -> cv.compareTo(v) == 0 }
            }

            ConditionOperator.NUMERIC_LESS_THAN -> {
                evaluateNumeric(contextValue, conditionValues) { cv, v -> cv < v }
            }

            ConditionOperator.NUMERIC_LESS_THAN_EQUALS -> {
                evaluateNumeric(contextValue, conditionValues) { cv, v -> cv <= v }
            }

            ConditionOperator.NUMERIC_GREATER_THAN -> {
                evaluateNumeric(contextValue, conditionValues) { cv, v -> cv > v }
            }

            ConditionOperator.NUMERIC_GREATER_THAN_EQUALS -> {
                evaluateNumeric(contextValue, conditionValues) { cv, v -> cv >= v }
            }

            // Date conditions
            ConditionOperator.DATE_EQUALS -> {
                evaluateDate(contextValue, conditionValues) { cv, v -> cv.isEqual(v) }
            }

            ConditionOperator.DATE_NOT_EQUALS -> {
                evaluateDateNone(contextValue, conditionValues) { cv, v -> cv.isEqual(v) }
            }

            ConditionOperator.DATE_LESS_THAN -> {
                evaluateDate(contextValue, conditionValues) { cv, v -> cv.isBefore(v) }
            }

            ConditionOperator.DATE_LESS_THAN_EQUALS -> {
                evaluateDate(contextValue, conditionValues) { cv, v -> !cv.isAfter(v) }
            }

            ConditionOperator.DATE_GREATER_THAN -> {
                evaluateDate(contextValue, conditionValues) { cv, v -> cv.isAfter(v) }
            }

            ConditionOperator.DATE_GREATER_THAN_EQUALS -> {
                evaluateDate(contextValue, conditionValues) { cv, v -> !cv.isBefore(v) }
            }

            // Boolean condition
            ConditionOperator.BOOL -> {
                val contextBool = contextValue?.toBooleanStrictOrNull()
                contextBool != null &&
                    conditionValues.any {
                        it.toBooleanStrictOrNull() == contextBool
                    }
            }

            // IP address conditions
            ConditionOperator.IP_ADDRESS -> {
                contextValue != null && conditionValues.any { matchesIpAddress(contextValue, it) }
            }

            ConditionOperator.NOT_IP_ADDRESS -> {
                contextValue != null && conditionValues.none { matchesIpAddress(contextValue, it) }
            }

            // Null (existence) condition
            ConditionOperator.NULL -> {
                // If condition value is "true", key should NOT exist
                // If condition value is "false", key SHOULD exist
                val expectNull = conditionValues.any { it.toBooleanStrictOrNull() == true }
                if (expectNull) {
                    contextValue == null
                } else {
                    contextValue != null
                }
            }
        }

    /**
     * Match a value against a wildcard pattern.
     * Supports `*` (any characters) and `?` (single character).
     */
    private fun matchesLike(
        value: String,
        pattern: String,
    ): Boolean {
        val regex =
            pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".")
        return Regex("^$regex$").matches(value)
    }

    /**
     * Evaluate numeric condition with any-match semantics.
     */
    private fun evaluateNumeric(
        contextValue: String?,
        conditionValues: List<String>,
        compare: (BigDecimal, BigDecimal) -> Boolean,
    ): Boolean {
        if (contextValue == null) return false
        val cv = contextValue.toBigDecimalOrNull() ?: return false
        return conditionValues.any { v ->
            v.toBigDecimalOrNull()?.let { compare(cv, it) } ?: false
        }
    }

    /**
     * Evaluate numeric condition with none-match semantics.
     */
    private fun evaluateNumericNone(
        contextValue: String?,
        conditionValues: List<String>,
        compare: (BigDecimal, BigDecimal) -> Boolean,
    ): Boolean {
        if (contextValue == null) return false
        val cv = contextValue.toBigDecimalOrNull() ?: return false
        return conditionValues.none { v ->
            v.toBigDecimalOrNull()?.let { compare(cv, it) } ?: false
        }
    }

    /**
     * Evaluate date condition with any-match semantics.
     */
    private fun evaluateDate(
        contextValue: String?,
        conditionValues: List<String>,
        compare: (OffsetDateTime, OffsetDateTime) -> Boolean,
    ): Boolean {
        if (contextValue == null) return false
        val cv = parseDateTime(contextValue) ?: return false
        return conditionValues.any { v ->
            parseDateTime(v)?.let { compare(cv, it) } ?: false
        }
    }

    /**
     * Evaluate date condition with none-match semantics.
     */
    private fun evaluateDateNone(
        contextValue: String?,
        conditionValues: List<String>,
        compare: (OffsetDateTime, OffsetDateTime) -> Boolean,
    ): Boolean {
        if (contextValue == null) return false
        val cv = parseDateTime(contextValue) ?: return false
        return conditionValues.none { v ->
            parseDateTime(v)?.let { compare(cv, it) } ?: false
        }
    }

    /**
     * Parse an ISO 8601 date-time string.
     */
    private fun parseDateTime(value: String): OffsetDateTime? =
        try {
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e: DateTimeParseException) {
            null
        }

    /**
     * Check if an IP address matches a CIDR pattern.
     */
    private fun matchesIpAddress(
        ip: String,
        cidr: String,
    ): Boolean {
        return try {
            val (network, prefixLength) =
                if (cidr.contains("/")) {
                    val parts = cidr.split("/")
                    parts[0] to parts[1].toInt()
                } else {
                    cidr to 32 // Exact match if no prefix
                }

            val ipAddr = InetAddress.getByName(ip)
            val networkAddr = InetAddress.getByName(network)

            val ipBytes = ipAddr.address
            val networkBytes = networkAddr.address

            // Different address families don't match
            if (ipBytes.size != networkBytes.size) return false

            var remainingBits = prefixLength
            for (i in ipBytes.indices) {
                if (remainingBits <= 0) break

                val bitsToCheck = minOf(8, remainingBits)
                val mask = (0xFF shl (8 - bitsToCheck)).toByte()

                if ((ipBytes[i].toInt() and mask.toInt()) != (networkBytes[i].toInt() and mask.toInt())) {
                    return false
                }

                remainingBits -= 8
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

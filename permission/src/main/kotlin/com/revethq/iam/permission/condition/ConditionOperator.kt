package com.revethq.iam.permission.condition

/**
 * Supported condition operators for policy evaluation.
 */
enum class ConditionOperator {
    // String conditions
    STRING_EQUALS,
    STRING_NOT_EQUALS,
    STRING_EQUALS_IGNORE_CASE,
    STRING_NOT_EQUALS_IGNORE_CASE,
    STRING_LIKE,
    STRING_NOT_LIKE,

    // Numeric conditions
    NUMERIC_EQUALS,
    NUMERIC_NOT_EQUALS,
    NUMERIC_LESS_THAN,
    NUMERIC_LESS_THAN_EQUALS,
    NUMERIC_GREATER_THAN,
    NUMERIC_GREATER_THAN_EQUALS,

    // Date conditions
    DATE_EQUALS,
    DATE_NOT_EQUALS,
    DATE_LESS_THAN,
    DATE_LESS_THAN_EQUALS,
    DATE_GREATER_THAN,
    DATE_GREATER_THAN_EQUALS,

    // Boolean condition
    BOOL,

    // IP address conditions
    IP_ADDRESS,
    NOT_IP_ADDRESS,

    // Existence condition
    NULL,

    ;

    companion object {
        private val nameMap =
            mapOf(
                "StringEquals" to STRING_EQUALS,
                "StringNotEquals" to STRING_NOT_EQUALS,
                "StringEqualsIgnoreCase" to STRING_EQUALS_IGNORE_CASE,
                "StringNotEqualsIgnoreCase" to STRING_NOT_EQUALS_IGNORE_CASE,
                "StringLike" to STRING_LIKE,
                "StringNotLike" to STRING_NOT_LIKE,
                "NumericEquals" to NUMERIC_EQUALS,
                "NumericNotEquals" to NUMERIC_NOT_EQUALS,
                "NumericLessThan" to NUMERIC_LESS_THAN,
                "NumericLessThanEquals" to NUMERIC_LESS_THAN_EQUALS,
                "NumericGreaterThan" to NUMERIC_GREATER_THAN,
                "NumericGreaterThanEquals" to NUMERIC_GREATER_THAN_EQUALS,
                "DateEquals" to DATE_EQUALS,
                "DateNotEquals" to DATE_NOT_EQUALS,
                "DateLessThan" to DATE_LESS_THAN,
                "DateLessThanEquals" to DATE_LESS_THAN_EQUALS,
                "DateGreaterThan" to DATE_GREATER_THAN,
                "DateGreaterThanEquals" to DATE_GREATER_THAN_EQUALS,
                "Bool" to BOOL,
                "IpAddress" to IP_ADDRESS,
                "NotIpAddress" to NOT_IP_ADDRESS,
                "Null" to NULL,
            )

        /**
         * Parse a condition operator from its string name (e.g., "StringEquals").
         * Returns null if the operator is not recognized.
         */
        fun fromName(name: String): ConditionOperator? = nameMap[name]
    }
}

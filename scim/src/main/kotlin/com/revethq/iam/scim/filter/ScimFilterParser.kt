package com.revethq.iam.scim.filter

import com.revethq.iam.scim.dtos.ScimError
import com.revethq.iam.scim.exception.ScimBadRequestException

object ScimFilterParser {
    private val SIMPLE_FILTER_REGEX =
        Regex(
            """(\w+(?:\.\w+)*)\s+(eq|co|sw)\s+"([^"]*)"""",
            RegexOption.IGNORE_CASE,
        )

    fun parse(filterString: String?): ScimFilter? {
        if (filterString.isNullOrBlank()) {
            return null
        }

        val trimmed = filterString.trim()

        // Handle simple single-expression filters (most common for Okta)
        val simpleMatch = SIMPLE_FILTER_REGEX.matchEntire(trimmed)
        if (simpleMatch != null) {
            return parseSimpleFilter(simpleMatch)
        }

        // Handle compound filters with 'and' / 'or'
        return parseCompoundFilter(trimmed)
    }

    private fun parseSimpleFilter(match: MatchResult): ScimFilter {
        val (attribute, operator, value) = match.destructured
        return when (operator.lowercase()) {
            "eq" -> EqFilter(attribute, value)

            "co" -> CoFilter(attribute, value)

            "sw" -> SwFilter(attribute, value)

            else -> throw ScimBadRequestException(
                ScimError.INVALID_FILTER,
                "Unsupported operator: $operator",
            )
        }
    }

    private fun parseCompoundFilter(filter: String): ScimFilter {
        // Simple compound filter parsing - split on 'and' / 'or' at top level
        // Note: This is a simplified parser that handles basic cases

        // Try to split on ' and ' (case insensitive)
        val andParts = splitOnOperator(filter, " and ")
        if (andParts.size > 1) {
            return andParts
                .map { parseCompoundFilter(it) }
                .reduce { left, right -> AndFilter(left, right) }
        }

        // Try to split on ' or ' (case insensitive)
        val orParts = splitOnOperator(filter, " or ")
        if (orParts.size > 1) {
            return orParts
                .map { parseCompoundFilter(it) }
                .reduce { left, right -> OrFilter(left, right) }
        }

        // Try as simple filter
        val simpleMatch = SIMPLE_FILTER_REGEX.matchEntire(filter.trim())
        if (simpleMatch != null) {
            return parseSimpleFilter(simpleMatch)
        }

        throw ScimBadRequestException(
            ScimError.INVALID_FILTER,
            "Invalid filter syntax: $filter",
        )
    }

    private fun splitOnOperator(
        filter: String,
        operator: String,
    ): List<String> {
        val parts = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < filter.length) {
            val char = filter[i]

            if (char == '"' && (i == 0 || filter[i - 1] != '\\')) {
                inQuotes = !inQuotes
                current.append(char)
                i++
            } else if (!inQuotes && filter.substring(i).lowercase().startsWith(operator.lowercase())) {
                parts.add(current.toString().trim())
                current = StringBuilder()
                i += operator.length
            } else {
                current.append(char)
                i++
            }
        }

        if (current.isNotBlank()) {
            parts.add(current.toString().trim())
        }

        return parts
    }
}

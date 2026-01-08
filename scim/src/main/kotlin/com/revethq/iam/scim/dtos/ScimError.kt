package com.revethq.iam.scim.dtos

data class ScimError(
    val schemas: List<String> = listOf(SCHEMA_ERROR),
    val status: String,
    val scimType: String? = null,
    val detail: String? = null
) {
    companion object {
        const val SCHEMA_ERROR = "urn:ietf:params:scim:api:messages:2.0:Error"

        // RFC 7644 Section 3.12 scimType values
        const val INVALID_FILTER = "invalidFilter"
        const val INVALID_SYNTAX = "invalidSyntax"
        const val INVALID_VALUE = "invalidValue"
        const val MUTABILITY = "mutability"
        const val UNIQUENESS = "uniqueness"
        const val NO_TARGET = "noTarget"
        const val TOO_MANY = "tooMany"
    }
}

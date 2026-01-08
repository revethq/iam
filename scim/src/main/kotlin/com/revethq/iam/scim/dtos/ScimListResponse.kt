package com.revethq.iam.scim.dtos

import jakarta.json.bind.annotation.JsonbProperty

data class ScimListResponse<T>(
    val schemas: List<String> = listOf(SCHEMA_LIST_RESPONSE),
    val totalResults: Int,
    val startIndex: Int,
    val itemsPerPage: Int,
    @get:JsonbProperty("Resources")
    val resources: List<T>
) {
    companion object {
        const val SCHEMA_LIST_RESPONSE = "urn:ietf:params:scim:api:messages:2.0:ListResponse"
    }
}

package com.revethq.iam.scim.dtos

import jakarta.json.bind.annotation.JsonbProperty

data class ScimMember(
    val value: String,
    val display: String? = null,
    val type: String = "User",
    @get:JsonbProperty("\$ref")
    val ref: String? = null
)

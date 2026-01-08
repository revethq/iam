package com.revethq.iam.scim.dtos

data class ScimEmail(
    val value: String,
    val type: String? = null,
    val primary: Boolean = false
)

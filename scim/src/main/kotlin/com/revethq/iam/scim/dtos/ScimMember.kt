package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.json.bind.annotation.JsonbProperty

@RegisterForReflection
data class ScimMember(
    val value: String,
    val display: String? = null,
    val type: String = "User",
    @get:JsonbProperty("\$ref")
    val ref: String? = null
)

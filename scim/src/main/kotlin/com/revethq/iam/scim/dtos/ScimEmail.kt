package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ScimEmail(
    val value: String,
    val type: String? = null,
    val primary: Boolean = false,
)

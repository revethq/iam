package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ScimName(
    val formatted: String? = null,
    val familyName: String? = null,
    val givenName: String? = null,
    val middleName: String? = null,
    val honorificPrefix: String? = null,
    val honorificSuffix: String? = null
)

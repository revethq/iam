package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.OffsetDateTime

@RegisterForReflection
data class ScimMeta(
    val resourceType: String,
    val created: OffsetDateTime? = null,
    val lastModified: OffsetDateTime? = null,
    val location: String? = null,
    val version: String? = null
)

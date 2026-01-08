package com.revethq.iam.scim.dtos

import java.time.OffsetDateTime

data class ScimMeta(
    val resourceType: String,
    val created: OffsetDateTime? = null,
    val lastModified: OffsetDateTime? = null,
    val location: String? = null,
    val version: String? = null
)

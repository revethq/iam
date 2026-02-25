package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ScimGroup(
    val schemas: List<String> = listOf(SCHEMA_GROUP),
    val id: String? = null,
    val externalId: String? = null,
    val meta: ScimMeta? = null,
    val displayName: String,
    val members: List<ScimMember>? = null,
) {
    companion object {
        const val SCHEMA_GROUP = "urn:ietf:params:scim:schemas:core:2.0:Group"
    }
}

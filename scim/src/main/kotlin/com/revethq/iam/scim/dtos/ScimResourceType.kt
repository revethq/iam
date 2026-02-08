package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ScimResourceType(
    val schemas: List<String> = listOf(SCHEMA_RESOURCE_TYPE),
    val id: String,
    val name: String,
    val endpoint: String,
    val description: String? = null,
    val schema: String,
    val schemaExtensions: List<SchemaExtension>? = null,
    val meta: ScimMeta? = null
) {
    companion object {
        const val SCHEMA_RESOURCE_TYPE = "urn:ietf:params:scim:schemas:core:2.0:ResourceType"
    }
}

@RegisterForReflection
data class SchemaExtension(
    val schema: String,
    val required: Boolean = false
)

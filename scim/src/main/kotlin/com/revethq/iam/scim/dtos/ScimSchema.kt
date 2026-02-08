package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class ScimSchema(
    val schemas: List<String> = listOf(SCHEMA_SCHEMA),
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val attributes: List<ScimSchemaAttribute> = emptyList(),
    val meta: ScimMeta? = null
) {
    companion object {
        const val SCHEMA_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Schema"
    }
}

@RegisterForReflection
data class ScimSchemaAttribute(
    val name: String,
    val type: String,
    val multiValued: Boolean = false,
    val description: String? = null,
    val required: Boolean = false,
    val caseExact: Boolean = false,
    val mutability: String = "readWrite",
    val returned: String = "default",
    val uniqueness: String = "none",
    val subAttributes: List<ScimSchemaAttribute>? = null,
    val referenceTypes: List<String>? = null,
    val canonicalValues: List<String>? = null
)

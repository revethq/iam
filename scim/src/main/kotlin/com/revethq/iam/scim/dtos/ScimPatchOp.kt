package com.revethq.iam.scim.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.json.bind.annotation.JsonbProperty

@RegisterForReflection
data class ScimPatchOp(
    val schemas: List<String> = listOf(SCHEMA_PATCH_OP),
    @get:JsonbProperty("Operations")
    val operations: List<ScimPatchOperation>
) {
    companion object {
        const val SCHEMA_PATCH_OP = "urn:ietf:params:scim:api:messages:2.0:PatchOp"
    }
}

@RegisterForReflection
data class ScimPatchOperation(
    val op: String,
    val path: String? = null,
    val value: Any? = null
)

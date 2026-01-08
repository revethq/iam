package com.revethq.iam.scim.dtos

data class ScimUser(
    val schemas: List<String> = listOf(SCHEMA_USER),
    val id: String? = null,
    val externalId: String? = null,
    val meta: ScimMeta? = null,
    val userName: String,
    val name: ScimName? = null,
    val displayName: String? = null,
    val emails: List<ScimEmail>? = null,
    val active: Boolean = true,
    val locale: String? = null,
    val password: String? = null
) {
    companion object {
        const val SCHEMA_USER = "urn:ietf:params:scim:schemas:core:2.0:User"
    }
}

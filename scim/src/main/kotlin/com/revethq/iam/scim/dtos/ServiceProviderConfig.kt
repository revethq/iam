package com.revethq.iam.scim.dtos

data class ServiceProviderConfig(
    val schemas: List<String> = listOf(SCHEMA_SERVICE_PROVIDER_CONFIG),
    val documentationUri: String? = null,
    val patch: SupportedFeature = SupportedFeature(supported = true),
    val bulk: BulkFeature = BulkFeature(supported = false),
    val filter: FilterFeature = FilterFeature(supported = true, maxResults = 200),
    val changePassword: SupportedFeature = SupportedFeature(supported = false),
    val sort: SupportedFeature = SupportedFeature(supported = false),
    val etag: SupportedFeature = SupportedFeature(supported = false),
    val authenticationSchemes: List<AuthenticationScheme> = listOf(
        AuthenticationScheme(
            type = "oauthbearertoken",
            name = "OAuth Bearer Token",
            description = "Authentication scheme using the OAuth Bearer Token Standard",
            specUri = "https://www.rfc-editor.org/info/rfc6750",
            primary = true
        )
    ),
    val meta: ScimMeta? = null
) {
    companion object {
        const val SCHEMA_SERVICE_PROVIDER_CONFIG = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"
    }
}

data class SupportedFeature(
    val supported: Boolean
)

data class BulkFeature(
    val supported: Boolean,
    val maxOperations: Int = 0,
    val maxPayloadSize: Int = 0
)

data class FilterFeature(
    val supported: Boolean,
    val maxResults: Int = 200
)

data class AuthenticationScheme(
    val type: String,
    val name: String,
    val description: String,
    val specUri: String? = null,
    val documentationUri: String? = null,
    val primary: Boolean = false
)

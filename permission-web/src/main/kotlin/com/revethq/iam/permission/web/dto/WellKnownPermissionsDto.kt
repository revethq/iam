package com.revethq.iam.permission.web.dto

data class PermissionDeclarationDto(
    val action: String,
    val description: String? = null,
    val resourceType: String? = null,
)

data class ServiceManifestDto(
    val service: String,
    val permissions: List<PermissionDeclarationDto>,
)

data class WellKnownPermissionsResponse(
    val manifests: List<ServiceManifestDto>,
)

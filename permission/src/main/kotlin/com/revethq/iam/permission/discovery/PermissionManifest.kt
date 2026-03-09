package com.revethq.iam.permission.discovery

data class PermissionManifest(
    val service: String,
    val permissions: List<PermissionDeclaration>,
)

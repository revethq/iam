package com.revethq.iam.permission.discovery

data class PermissionDeclaration(
    val action: String,
    val description: String? = null,
    val resourceType: String? = null,
)

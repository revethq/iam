package com.revethq.iam.permission.discovery

interface PermissionProvider {
    fun manifest(): PermissionManifest
}

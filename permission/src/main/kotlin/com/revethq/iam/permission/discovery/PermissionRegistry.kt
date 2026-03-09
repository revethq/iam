package com.revethq.iam.permission.discovery

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import jakarta.inject.Inject

@ApplicationScoped
class PermissionRegistry
    @Inject
    constructor(
        private val providers: Instance<PermissionProvider>,
    ) {
        fun allManifests(): List<PermissionManifest> = providers.map { it.manifest() }

        fun allPermissions(): List<PermissionDeclaration> = providers.flatMap { it.manifest().permissions }
    }

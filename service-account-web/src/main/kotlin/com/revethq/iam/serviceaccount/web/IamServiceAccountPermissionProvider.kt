package com.revethq.iam.serviceaccount.web

import com.revethq.iam.permission.discovery.Actions
import com.revethq.iam.permission.discovery.PermissionDeclaration
import com.revethq.iam.permission.discovery.PermissionManifest
import com.revethq.iam.permission.discovery.PermissionProvider
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class IamServiceAccountPermissionProvider : PermissionProvider {
    override fun manifest() =
        PermissionManifest(
            service = Actions.SERVICE,
            permissions =
                listOf(
                    // Service Account
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.CREATE,
                        description = "Create a new service account",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.GET,
                        description = "Get a service account by ID",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/{serviceAccountId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.LIST,
                        description = "List service accounts",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.UPDATE,
                        description = "Update an existing service account",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/{serviceAccountId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.DELETE,
                        description = "Delete a service account",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/{serviceAccountId}",
                    ),
                    // Service Account Policies
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.LIST_POLICIES,
                        description = "List policies attached to a service account",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/{serviceAccountId}",
                    ),
                    // Service Account Profile
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.GET_PROFILE,
                        description = "Get the profile of a service account",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/{serviceAccountId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.ServiceAccount.SET_PROFILE,
                        description = "Set the profile of a service account",
                        resourceType = "urn:revet:iam:{tenantId}:service-account/{serviceAccountId}",
                    ),
                ),
        )
}

package com.revethq.iam.scim

import com.revethq.iam.permission.discovery.Actions
import com.revethq.iam.permission.discovery.PermissionDeclaration
import com.revethq.iam.permission.discovery.PermissionManifest
import com.revethq.iam.permission.discovery.PermissionProvider
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class IamScimPermissionProvider : PermissionProvider {
    override fun manifest() =
        PermissionManifest(
            service = "iam-scim",
            permissions =
                listOf(
                    // SCIM User
                    PermissionDeclaration(
                        action = Actions.Scim.CREATE_USER,
                        description = "Create a user via SCIM provisioning",
                        resourceType = "urn:revet:iam:{tenantId}:user/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.GET_USER,
                        description = "Get a user via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.LIST_USERS,
                        description = "List users via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:user/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.REPLACE_USER,
                        description = "Replace a user via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.UPDATE_USER,
                        description = "Partially update a user via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.DELETE_USER,
                        description = "Delete a user via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    // SCIM Group
                    PermissionDeclaration(
                        action = Actions.Scim.CREATE_GROUP,
                        description = "Create a group via SCIM provisioning",
                        resourceType = "urn:revet:iam:{tenantId}:group/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.GET_GROUP,
                        description = "Get a group via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.LIST_GROUPS,
                        description = "List groups via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:group/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.REPLACE_GROUP,
                        description = "Replace a group via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.UPDATE_GROUP,
                        description = "Partially update a group via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Scim.DELETE_GROUP,
                        description = "Delete a group via SCIM",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                ),
        )
}

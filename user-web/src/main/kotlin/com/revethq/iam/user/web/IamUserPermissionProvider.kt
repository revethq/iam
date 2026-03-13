package com.revethq.iam.user.web

import com.revethq.iam.permission.discovery.Actions
import com.revethq.iam.permission.discovery.PermissionDeclaration
import com.revethq.iam.permission.discovery.PermissionManifest
import com.revethq.iam.permission.discovery.PermissionProvider
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class IamUserPermissionProvider : PermissionProvider {
    override fun manifest() =
        PermissionManifest(
            service = "iam-users",
            permissions =
                listOf(
                    // User
                    PermissionDeclaration(
                        action = Actions.User.CREATE,
                        description = "Create a new user",
                        resourceType = "urn:revet:iam:{tenantId}:user/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.GET,
                        description = "Get a user by ID",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.LIST,
                        description = "List users",
                        resourceType = "urn:revet:iam:{tenantId}:user/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.UPDATE,
                        description = "Update an existing user",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.DELETE,
                        description = "Delete a user",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    // Group
                    PermissionDeclaration(
                        action = Actions.Group.CREATE,
                        description = "Create a new group",
                        resourceType = "urn:revet:iam:{tenantId}:group/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.GET,
                        description = "Get a group by ID",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.LIST,
                        description = "List groups",
                        resourceType = "urn:revet:iam:{tenantId}:group/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.UPDATE,
                        description = "Update an existing group",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.DELETE,
                        description = "Delete a group",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.LIST_MEMBERS,
                        description = "List members of a group",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.ADD_MEMBER,
                        description = "Add a member to a group",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Group.REMOVE_MEMBER,
                        description = "Remove a member from a group",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                ),
        )
}

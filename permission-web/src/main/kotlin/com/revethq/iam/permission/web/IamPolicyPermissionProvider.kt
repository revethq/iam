package com.revethq.iam.permission.web

import com.revethq.iam.permission.discovery.Actions
import com.revethq.iam.permission.discovery.PermissionDeclaration
import com.revethq.iam.permission.discovery.PermissionManifest
import com.revethq.iam.permission.discovery.PermissionProvider
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class IamPolicyPermissionProvider : PermissionProvider {
    override fun manifest() =
        PermissionManifest(
            service = "iam-permissions",
            permissions =
                listOf(
                    // Policy
                    PermissionDeclaration(
                        action = Actions.Policy.CREATE,
                        description = "Create a new policy",
                        resourceType = "urn:revet:iam:{tenantId}:policy/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.GET,
                        description = "Get a policy by ID",
                        resourceType = "urn:revet:iam:{tenantId}:policy/{policyId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.LIST,
                        description = "List policies",
                        resourceType = "urn:revet:iam:{tenantId}:policy/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.UPDATE,
                        description = "Update an existing policy",
                        resourceType = "urn:revet:iam:{tenantId}:policy/{policyId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.DELETE,
                        description = "Delete a policy",
                        resourceType = "urn:revet:iam:{tenantId}:policy/{policyId}",
                    ),
                    // Policy Attachment
                    PermissionDeclaration(
                        action = Actions.Policy.ATTACH,
                        description = "Attach a policy to a principal",
                        resourceType = "urn:revet:iam:{tenantId}:policy/{policyId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.DETACH,
                        description = "Detach a policy from a principal",
                        resourceType = "urn:revet:iam:{tenantId}:policy/{policyId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.LIST_ATTACHMENTS,
                        description = "List principals attached to a policy",
                        resourceType = "urn:revet:iam:{tenantId}:policy/{policyId}",
                    ),
                    // User/Group policy listing
                    PermissionDeclaration(
                        action = Actions.Policy.LIST_USER_POLICIES,
                        description = "List policies attached to a user",
                        resourceType = "urn:revet:iam:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Policy.LIST_GROUP_POLICIES,
                        description = "List policies attached to a group",
                        resourceType = "urn:revet:iam:{tenantId}:group/{groupId}",
                    ),
                ),
        )
}

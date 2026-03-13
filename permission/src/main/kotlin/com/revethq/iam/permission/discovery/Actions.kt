package com.revethq.iam.permission.discovery

object Actions {
    const val SERVICE = "iam"

    object User {
        const val CREATE = "$SERVICE:CreateUser"
        const val GET = "$SERVICE:GetUser"
        const val LIST = "$SERVICE:ListUsers"
        const val UPDATE = "$SERVICE:UpdateUser"
        const val DELETE = "$SERVICE:DeleteUser"

        val ALL = listOf(CREATE, GET, LIST, UPDATE, DELETE)
        val READ_ONLY = listOf(GET, LIST)
    }

    object Group {
        const val CREATE = "$SERVICE:CreateGroup"
        const val GET = "$SERVICE:GetGroup"
        const val LIST = "$SERVICE:ListGroups"
        const val UPDATE = "$SERVICE:UpdateGroup"
        const val DELETE = "$SERVICE:DeleteGroup"
        const val LIST_MEMBERS = "$SERVICE:ListGroupMembers"
        const val ADD_MEMBER = "$SERVICE:AddGroupMember"
        const val REMOVE_MEMBER = "$SERVICE:RemoveGroupMember"

        val ALL = listOf(CREATE, GET, LIST, UPDATE, DELETE, LIST_MEMBERS, ADD_MEMBER, REMOVE_MEMBER)
        val READ_ONLY = listOf(GET, LIST, LIST_MEMBERS)
    }

    object Policy {
        const val CREATE = "$SERVICE:CreatePolicy"
        const val GET = "$SERVICE:GetPolicy"
        const val LIST = "$SERVICE:ListPolicies"
        const val UPDATE = "$SERVICE:UpdatePolicy"
        const val DELETE = "$SERVICE:DeletePolicy"
        const val ATTACH = "$SERVICE:AttachPolicy"
        const val DETACH = "$SERVICE:DetachPolicy"
        const val LIST_ATTACHMENTS = "$SERVICE:ListPolicyAttachments"
        const val LIST_USER_POLICIES = "$SERVICE:ListUserPolicies"
        const val LIST_GROUP_POLICIES = "$SERVICE:ListGroupPolicies"

        val ALL =
            listOf(
                CREATE,
                GET,
                LIST,
                UPDATE,
                DELETE,
                ATTACH,
                DETACH,
                LIST_ATTACHMENTS,
                LIST_USER_POLICIES,
                LIST_GROUP_POLICIES,
            )
        val READ_ONLY = listOf(GET, LIST, LIST_ATTACHMENTS, LIST_USER_POLICIES, LIST_GROUP_POLICIES)
    }

    object ServiceAccount {
        const val CREATE = "$SERVICE:CreateServiceAccount"
        const val GET = "$SERVICE:GetServiceAccount"
        const val LIST = "$SERVICE:ListServiceAccounts"
        const val UPDATE = "$SERVICE:UpdateServiceAccount"
        const val DELETE = "$SERVICE:DeleteServiceAccount"
        const val LIST_POLICIES = "$SERVICE:ListServiceAccountPolicies"
        const val GET_PROFILE = "$SERVICE:GetServiceAccountProfile"
        const val SET_PROFILE = "$SERVICE:SetServiceAccountProfile"

        val ALL = listOf(CREATE, GET, LIST, UPDATE, DELETE, LIST_POLICIES, GET_PROFILE, SET_PROFILE)
        val READ_ONLY = listOf(GET, LIST, LIST_POLICIES, GET_PROFILE)
    }

    object Scim {
        const val CREATE_USER = "$SERVICE:ScimCreateUser"
        const val GET_USER = "$SERVICE:ScimGetUser"
        const val LIST_USERS = "$SERVICE:ScimListUsers"
        const val REPLACE_USER = "$SERVICE:ScimReplaceUser"
        const val UPDATE_USER = "$SERVICE:ScimUpdateUser"
        const val DELETE_USER = "$SERVICE:ScimDeleteUser"
        const val CREATE_GROUP = "$SERVICE:ScimCreateGroup"
        const val GET_GROUP = "$SERVICE:ScimGetGroup"
        const val LIST_GROUPS = "$SERVICE:ScimListGroups"
        const val REPLACE_GROUP = "$SERVICE:ScimReplaceGroup"
        const val UPDATE_GROUP = "$SERVICE:ScimUpdateGroup"
        const val DELETE_GROUP = "$SERVICE:ScimDeleteGroup"

        val ALL =
            listOf(
                CREATE_USER,
                GET_USER,
                LIST_USERS,
                REPLACE_USER,
                UPDATE_USER,
                DELETE_USER,
                CREATE_GROUP,
                GET_GROUP,
                LIST_GROUPS,
                REPLACE_GROUP,
                UPDATE_GROUP,
                DELETE_GROUP,
            )
    }

    const val ALL_ACTIONS = "$SERVICE:*"
    const val GLOBAL_WILDCARD = "*"
}

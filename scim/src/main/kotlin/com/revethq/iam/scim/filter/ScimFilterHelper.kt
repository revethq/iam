package com.revethq.iam.scim.filter

import com.revethq.iam.scim.dtos.ScimGroup
import com.revethq.iam.scim.dtos.ScimUser
import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.Page
import com.revethq.iam.user.persistence.service.GroupService
import com.revethq.iam.user.persistence.service.UserService
import java.util.UUID

object ScimFilterHelper {

    /**
     * Apply filter to users. For simple eq filters on indexed fields,
     * uses repository methods directly. Falls back to in-memory filtering.
     */
    fun filterUsers(
        filterString: String?,
        userService: UserService,
        identityProviderId: UUID,
        startIndex: Int,
        count: Int
    ): FilteredResult<User> {
        val filter = ScimFilterParser.parse(filterString)

        if (filter == null) {
            val page = userService.list(startIndex, count)
            return FilteredResult(page.items, page.totalCount)
        }

        // Try to use indexed repository methods for simple eq filters
        if (filter is EqFilter) {
            when (filter.attribute.lowercase()) {
                "username" -> {
                    val user = userService.findByUsername(filter.value)
                    return FilteredResult(listOfNotNull(user), if (user != null) 1 else 0)
                }
                "externalid" -> {
                    val user = userService.findByExternalId(filter.value, identityProviderId)
                    return FilteredResult(listOfNotNull(user), if (user != null) 1 else 0)
                }
                "emails.value" -> {
                    val user = userService.findByEmail(filter.value)
                    return FilteredResult(listOfNotNull(user), if (user != null) 1 else 0)
                }
            }
        }

        // Fall back to in-memory filtering
        val allUsers = userService.list(0, Int.MAX_VALUE)
        val filtered = allUsers.items.filter { user ->
            val attrs = userToAttributes(user, userService, identityProviderId)
            filter.matches(attrs)
        }

        val paged = filtered.drop(startIndex).take(count)
        return FilteredResult(paged, filtered.size.toLong())
    }

    /**
     * Apply filter to groups. For simple eq filters on indexed fields,
     * uses repository methods directly. Falls back to in-memory filtering.
     */
    fun filterGroups(
        filterString: String?,
        groupService: GroupService,
        startIndex: Int,
        count: Int
    ): FilteredResult<Group> {
        val filter = ScimFilterParser.parse(filterString)

        if (filter == null) {
            val page = groupService.list(startIndex, count)
            return FilteredResult(page.items, page.totalCount)
        }

        // Try to use indexed repository methods for simple eq filters
        if (filter is EqFilter) {
            when (filter.attribute.lowercase()) {
                "displayname" -> {
                    val group = groupService.findByDisplayName(filter.value)
                    return FilteredResult(listOfNotNull(group), if (group != null) 1 else 0)
                }
                "externalid" -> {
                    val group = groupService.findByExternalId(filter.value)
                    return FilteredResult(listOfNotNull(group), if (group != null) 1 else 0)
                }
            }
        }

        // Fall back to in-memory filtering
        val allGroups = groupService.list(0, Int.MAX_VALUE)
        val filtered = allGroups.items.filter { group ->
            val attrs = groupToAttributes(group)
            filter.matches(attrs)
        }

        val paged = filtered.drop(startIndex).take(count)
        return FilteredResult(paged, filtered.size.toLong())
    }

    private fun userToAttributes(user: User, userService: UserService, identityProviderId: UUID): Map<String, Any?> {
        val props = user.metadata.properties.orEmpty()
        val externalId = userService.getExternalId(user.id, identityProviderId)
        return mapOf(
            "id" to user.id.toString(),
            "userName" to user.username,
            "externalId" to externalId,
            "emails" to listOf(mapOf("value" to user.email, "primary" to true)),
            "displayName" to props["displayName"],
            "active" to (props["active"] ?: true)
        )
    }

    private fun groupToAttributes(group: Group): Map<String, Any?> {
        return mapOf(
            "id" to group.id.toString(),
            "displayName" to group.displayName,
            "externalId" to group.externalId
        )
    }

    data class FilteredResult<T>(
        val items: List<T>,
        val totalCount: Long
    )
}

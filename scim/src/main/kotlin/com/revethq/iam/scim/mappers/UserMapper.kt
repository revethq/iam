package com.revethq.iam.scim.mappers

import com.revethq.core.Metadata
import com.revethq.iam.scim.dtos.ScimEmail
import com.revethq.iam.scim.dtos.ScimMeta
import com.revethq.iam.scim.dtos.ScimUser
import com.revethq.iam.user.domain.User
import java.util.UUID

fun User.toScimUser(
    baseUrl: String,
    externalId: String? = null,
): ScimUser {
    val props = metadata.properties.orEmpty()
    return ScimUser(
        id = id.toString(),
        externalId = externalId,
        meta =
            ScimMeta(
                resourceType = "User",
                created = createdOn,
                lastModified = updatedOn,
                location = "$baseUrl/scim/v2/Users/$id",
            ),
        userName = username,
        displayName = props["displayName"] as? String,
        emails =
            listOf(
                ScimEmail(
                    value = email,
                    type = "work",
                    primary = true,
                ),
            ),
        active = props["active"] as? Boolean ?: true,
        locale = props["locale"] as? String,
    )
}

fun ScimUser.toDomain(): User {
    val props = mutableMapOf<String, Any>()
    displayName?.let { props["displayName"] = it }
    props["active"] = active
    locale?.let { props["locale"] = it }

    return User(
        id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
        username = userName,
        email =
            emails?.firstOrNull { it.primary }?.value
                ?: emails?.firstOrNull()?.value
                ?: throw IllegalArgumentException("User must have at least one email"),
        metadata = Metadata(properties = props),
    )
}

fun ScimUser.updateDomain(existing: User): User {
    val props =
        existing.metadata.properties
            .orEmpty()
            .toMutableMap()
    displayName?.let { props["displayName"] = it }
    props["active"] = active
    locale?.let { props["locale"] = it }

    return existing.copy(
        username = userName,
        email =
            emails?.firstOrNull { it.primary }?.value
                ?: emails?.firstOrNull()?.value
                ?: existing.email,
        metadata = existing.metadata.copy(properties = props),
    )
}

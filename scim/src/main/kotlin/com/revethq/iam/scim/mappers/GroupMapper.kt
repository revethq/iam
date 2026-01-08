package com.revethq.iam.scim.mappers

import com.revethq.iam.scim.dtos.ScimGroup
import com.revethq.iam.scim.dtos.ScimMember
import com.revethq.iam.scim.dtos.ScimMeta
import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import com.revethq.iam.user.domain.User
import java.util.UUID

fun Group.toScimGroup(baseUrl: String, members: List<Pair<GroupMember, User?>> = emptyList()): ScimGroup {
    return ScimGroup(
        id = id.toString(),
        externalId = externalId,
        meta = ScimMeta(
            resourceType = "Group",
            created = createdOn,
            lastModified = updatedOn,
            location = "$baseUrl/scim/v2/Groups/$id"
        ),
        displayName = displayName,
        members = members.map { (member, user) ->
            ScimMember(
                value = member.memberId.toString(),
                display = user?.username,
                type = when (member.memberType) {
                    MemberType.USER -> "User"
                    MemberType.GROUP -> "Group"
                },
                ref = when (member.memberType) {
                    MemberType.USER -> "$baseUrl/scim/v2/Users/${member.memberId}"
                    MemberType.GROUP -> "$baseUrl/scim/v2/Groups/${member.memberId}"
                }
            )
        }.ifEmpty { null }
    )
}

fun ScimGroup.toDomain(): Group {
    return Group(
        id = id?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
        displayName = displayName,
        externalId = externalId,
        metadata = com.revethq.core.Metadata()
    )
}

fun ScimGroup.updateDomain(existing: Group): Group {
    return existing.copy(
        displayName = displayName,
        externalId = externalId ?: existing.externalId
    )
}

fun ScimMember.toGroupMember(groupId: UUID): GroupMember {
    return GroupMember(
        groupId = groupId,
        memberId = UUID.fromString(value),
        memberType = when (type.lowercase()) {
            "user" -> MemberType.USER
            "group" -> MemberType.GROUP
            else -> MemberType.USER
        }
    )
}

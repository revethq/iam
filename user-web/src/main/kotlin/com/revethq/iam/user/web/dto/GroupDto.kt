package com.revethq.iam.user.web.dto

import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import java.time.OffsetDateTime
import java.util.UUID

data class CreateGroupRequest(
    val displayName: String,
) {
    fun toDomain(): Group =
        Group(
            id = UUID.randomUUID(),
            displayName = displayName,
        )
}

data class UpdateGroupRequest(
    val displayName: String,
)

data class GroupResponse(
    val id: UUID,
    val displayName: String,
    val createdOn: OffsetDateTime?,
    val updatedOn: OffsetDateTime?,
) {
    companion object {
        fun fromDomain(group: Group): GroupResponse =
            GroupResponse(
                id = group.id,
                displayName = group.displayName,
                createdOn = group.createdOn,
                updatedOn = group.updatedOn,
            )
    }
}

data class AddMemberRequest(
    val memberId: UUID,
    val memberType: MemberType = MemberType.USER,
) {
    fun toDomain(groupId: UUID): GroupMember =
        GroupMember(
            groupId = groupId,
            memberId = memberId,
            memberType = memberType,
        )
}

data class GroupMemberResponse(
    val id: UUID?,
    val memberId: UUID,
    val memberType: MemberType,
    val createdOn: OffsetDateTime?,
) {
    companion object {
        fun fromDomain(member: GroupMember): GroupMemberResponse =
            GroupMemberResponse(
                id = member.id,
                memberId = member.memberId,
                memberType = member.memberType,
                createdOn = member.createdOn,
            )
    }
}

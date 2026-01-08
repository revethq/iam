package com.revethq.iam.user.persistence.entity

import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_group_members")
class GroupMemberEntity {

    @Id
    lateinit var id: UUID

    @Column(name = "group_id", nullable = false)
    lateinit var groupId: UUID

    @Column(name = "member_id", nullable = false)
    lateinit var memberId: UUID

    @Column(name = "member_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var memberType: MemberType = MemberType.USER

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    fun toDomain(): GroupMember = GroupMember(
        id = id,
        groupId = groupId,
        memberId = memberId,
        memberType = memberType,
        createdOn = createdOn
    )

    companion object {
        fun fromDomain(member: GroupMember): GroupMemberEntity = GroupMemberEntity().apply {
            val now = OffsetDateTime.now()
            id = member.id ?: UUID.randomUUID()
            groupId = member.groupId
            memberId = member.memberId
            memberType = member.memberType
            createdOn = member.createdOn ?: now
        }
    }
}

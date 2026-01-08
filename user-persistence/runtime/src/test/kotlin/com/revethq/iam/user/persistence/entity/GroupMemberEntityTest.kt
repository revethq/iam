package com.revethq.iam.user.persistence.entity

import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GroupMemberEntityTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val entity = GroupMemberEntity().apply {
            id = UUID.randomUUID()
            groupId = UUID.randomUUID()
            memberId = UUID.randomUUID()
            memberType = MemberType.USER
            createdOn = OffsetDateTime.now()
        }

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.groupId, domain.groupId)
        assertEquals(entity.memberId, domain.memberId)
        assertEquals(entity.memberType, domain.memberType)
        assertEquals(entity.createdOn, domain.createdOn)
    }

    @Test
    fun `toDomain handles GROUP member type`() {
        val entity = GroupMemberEntity().apply {
            id = UUID.randomUUID()
            groupId = UUID.randomUUID()
            memberId = UUID.randomUUID()
            memberType = MemberType.GROUP
            createdOn = OffsetDateTime.now()
        }

        val domain = entity.toDomain()

        assertEquals(MemberType.GROUP, domain.memberType)
    }

    @Test
    fun `fromDomain maps all fields correctly`() {
        val now = OffsetDateTime.now()
        val member = GroupMember(
            id = UUID.randomUUID(),
            groupId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            memberType = MemberType.USER,
            createdOn = now
        )

        val entity = GroupMemberEntity.fromDomain(member)

        assertEquals(member.id, entity.id)
        assertEquals(member.groupId, entity.groupId)
        assertEquals(member.memberId, entity.memberId)
        assertEquals(member.memberType, entity.memberType)
        assertEquals(member.createdOn, entity.createdOn)
    }

    @Test
    fun `fromDomain generates id when null`() {
        val member = GroupMember(
            id = null,
            groupId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            memberType = MemberType.USER,
            createdOn = null
        )

        val entity = GroupMemberEntity.fromDomain(member)

        assertNotNull(entity.id)
    }

    @Test
    fun `fromDomain sets createdOn when null`() {
        val member = GroupMember(
            groupId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            createdOn = null
        )

        val entity = GroupMemberEntity.fromDomain(member)

        assertNotNull(entity.createdOn)
    }

    @Test
    fun `fromDomain preserves GROUP member type`() {
        val member = GroupMember(
            groupId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            memberType = MemberType.GROUP
        )

        val entity = GroupMemberEntity.fromDomain(member)

        assertEquals(MemberType.GROUP, entity.memberType)
    }

    @Test
    fun `fromDomain defaults to USER member type`() {
        val member = GroupMember(
            groupId = UUID.randomUUID(),
            memberId = UUID.randomUUID()
        )

        val entity = GroupMemberEntity.fromDomain(member)

        assertEquals(MemberType.USER, entity.memberType)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = GroupMember(
            id = UUID.randomUUID(),
            groupId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            memberType = MemberType.GROUP,
            createdOn = OffsetDateTime.now().minusHours(5)
        )

        val entity = GroupMemberEntity.fromDomain(original)
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.groupId, restored.groupId)
        assertEquals(original.memberId, restored.memberId)
        assertEquals(original.memberType, restored.memberType)
        assertEquals(original.createdOn, restored.createdOn)
    }
}

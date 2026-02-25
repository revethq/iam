package com.revethq.iam.user.persistence.service

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import com.revethq.iam.user.persistence.entity.GroupEntity
import com.revethq.iam.user.persistence.entity.GroupMemberEntity
import com.revethq.iam.user.persistence.repository.GroupMemberRepository
import com.revethq.iam.user.persistence.repository.GroupRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GroupServiceImplTest {
    private val groupRepository = mockk<GroupRepository>(relaxed = true)
    private val groupMemberRepository = mockk<GroupMemberRepository>(relaxed = true)
    private val groupService = GroupServiceImpl(groupRepository, groupMemberRepository)

    @Test
    fun `create persists group and returns domain object`() {
        val group =
            Group(
                id = UUID.randomUUID(),
                displayName = "Engineering",
            )

        val result = groupService.create(group)

        verify { groupRepository.persist(any<GroupEntity>()) }
        assertEquals(group.displayName, result.displayName)
    }

    @Test
    fun `findById returns group when found`() {
        val groupId = UUID.randomUUID()
        val entity = createGroupEntity(groupId, "Found Group")
        every { groupRepository.findById(groupId) } returns entity

        val result = groupService.findById(groupId)

        assertNotNull(result)
        assertEquals("Found Group", result.displayName)
    }

    @Test
    fun `findById returns null when not found`() {
        val groupId = UUID.randomUUID()
        every { groupRepository.findById(groupId) } returns null

        val result = groupService.findById(groupId)

        assertNull(result)
    }

    @Test
    fun `findByExternalId returns group when found`() {
        val entity = createGroupEntity(UUID.randomUUID(), "External Group", "ext-123")
        every { groupRepository.findByExternalId("ext-123") } returns entity

        val result = groupService.findByExternalId("ext-123")

        assertNotNull(result)
        assertEquals("External Group", result.displayName)
    }

    @Test
    fun `findByExternalId returns null when not found`() {
        every { groupRepository.findByExternalId("unknown") } returns null

        val result = groupService.findByExternalId("unknown")

        assertNull(result)
    }

    @Test
    fun `findByDisplayName returns group when found`() {
        val entity = createGroupEntity(UUID.randomUUID(), "Marketing")
        every { groupRepository.findByDisplayName("Marketing") } returns entity

        val result = groupService.findByDisplayName("Marketing")

        assertNotNull(result)
        assertEquals("Marketing", result.displayName)
    }

    @Test
    fun `update modifies existing group`() {
        val groupId = UUID.randomUUID()
        val existingEntity = createGroupEntity(groupId, "Old Name")
        every { groupRepository.findById(groupId) } returns existingEntity

        val updatedGroup =
            Group(
                id = groupId,
                displayName = "New Name",
                externalId = "new-ext-id",
            )

        val result = groupService.update(updatedGroup)

        assertEquals("New Name", existingEntity.displayName)
        assertEquals("new-ext-id", existingEntity.externalId)
        assertNotNull(result)
    }

    @Test
    fun `delete removes group and members`() {
        val groupId = UUID.randomUUID()
        every { groupMemberRepository.deleteByGroupId(groupId) } returns 3L
        every { groupRepository.deleteById(groupId) } returns true

        val result = groupService.delete(groupId)

        verify { groupMemberRepository.deleteByGroupId(groupId) }
        verify { groupRepository.deleteById(groupId) }
        assertTrue(result)
    }

    @Test
    fun `count returns repository count`() {
        every { groupRepository.count() } returns 15L

        val result = groupService.count()

        assertEquals(15L, result)
    }

    @Test
    fun `getMembers returns list of members`() {
        val groupId = UUID.randomUUID()
        val memberEntities =
            listOf(
                createMemberEntity(groupId, UUID.randomUUID(), MemberType.USER),
                createMemberEntity(groupId, UUID.randomUUID(), MemberType.USER),
            )
        every { groupMemberRepository.findByGroupId(groupId) } returns memberEntities

        val result = groupService.getMembers(groupId)

        assertEquals(2, result.size)
    }

    @Test
    fun `addMember persists new member`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val member =
            GroupMember(
                groupId = groupId,
                memberId = memberId,
                memberType = MemberType.USER,
            )

        val result = groupService.addMember(groupId, member)

        verify { groupMemberRepository.persist(any<GroupMemberEntity>()) }
        assertEquals(groupId, result.groupId)
        assertEquals(memberId, result.memberId)
    }

    @Test
    fun `removeMember deletes member when found`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val memberEntity = createMemberEntity(groupId, memberId, MemberType.USER)
        every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns memberEntity

        val result = groupService.removeMember(groupId, memberId)

        verify { groupMemberRepository.delete(memberEntity) }
        assertTrue(result)
    }

    @Test
    fun `removeMember returns false when not found`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        every { groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId) } returns null

        val result = groupService.removeMember(groupId, memberId)

        assertFalse(result)
    }

    @Test
    fun `setMembers replaces all members`() {
        val groupId = UUID.randomUUID()
        val newMembers =
            listOf(
                GroupMember(groupId = groupId, memberId = UUID.randomUUID()),
                GroupMember(groupId = groupId, memberId = UUID.randomUUID()),
            )
        every { groupMemberRepository.deleteByGroupId(groupId) } returns 1L

        val result = groupService.setMembers(groupId, newMembers)

        verify { groupMemberRepository.deleteByGroupId(groupId) }
        verify(exactly = 2) { groupMemberRepository.persist(any<GroupMemberEntity>()) }
        assertEquals(2, result.size)
    }

    @Test
    fun `setMembers with empty list clears all members`() {
        val groupId = UUID.randomUUID()
        every { groupMemberRepository.deleteByGroupId(groupId) } returns 5L

        val result = groupService.setMembers(groupId, emptyList())

        verify { groupMemberRepository.deleteByGroupId(groupId) }
        assertTrue(result.isEmpty())
    }

    private fun createGroupEntity(
        id: UUID,
        displayName: String,
        externalId: String? = null,
    ): GroupEntity =
        GroupEntity().apply {
            this.id = id
            this.displayName = displayName
            this.externalId = externalId
            this.metadata = Metadata()
            this.createdOn = OffsetDateTime.now()
            this.updatedOn = OffsetDateTime.now()
        }

    private fun createMemberEntity(
        groupId: UUID,
        memberId: UUID,
        memberType: MemberType,
    ): GroupMemberEntity =
        GroupMemberEntity().apply {
            this.id = UUID.randomUUID()
            this.groupId = groupId
            this.memberId = memberId
            this.memberType = memberType
            this.createdOn = OffsetDateTime.now()
        }
}

package com.revethq.iam.scim.mappers

import com.revethq.core.Metadata
import com.revethq.iam.scim.dtos.ScimGroup
import com.revethq.iam.scim.dtos.ScimMember
import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import com.revethq.iam.user.domain.User
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GroupMapperTest {
    private val baseUrl = "https://example.com"

    @Test
    fun `toScimGroup maps basic fields`() {
        val groupId = UUID.randomUUID()
        val now = OffsetDateTime.now()
        val group =
            Group(
                id = groupId,
                displayName = "Developers",
                externalId = "ext-group-123",
                metadata = Metadata(),
                createdOn = now,
                updatedOn = now,
            )

        val scimGroup = group.toScimGroup(baseUrl)

        assertEquals(groupId.toString(), scimGroup.id)
        assertEquals("Developers", scimGroup.displayName)
        assertEquals("ext-group-123", scimGroup.externalId)
        assertNull(scimGroup.members)
    }

    @Test
    fun `toScimGroup includes meta`() {
        val groupId = UUID.randomUUID()
        val group =
            Group(
                id = groupId,
                displayName = "Admins",
            )

        val scimGroup = group.toScimGroup(baseUrl)

        assertNotNull(scimGroup.meta)
        assertEquals("Group", scimGroup.meta!!.resourceType)
        assertEquals("$baseUrl/scim/v2/Groups/$groupId", scimGroup.meta!!.location)
    }

    @Test
    fun `toScimGroup includes members with user details`() {
        val groupId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val group =
            Group(
                id = groupId,
                displayName = "Developers",
            )

        val member =
            GroupMember(
                groupId = groupId,
                memberId = userId,
                memberType = MemberType.USER,
            )

        val user =
            User(
                id = userId,
                username = "john.doe",
                email = "john@example.com",
            )

        val scimGroup = group.toScimGroup(baseUrl, listOf(member to user))

        assertNotNull(scimGroup.members)
        assertEquals(1, scimGroup.members!!.size)
        assertEquals(userId.toString(), scimGroup.members!![0].value)
        assertEquals("john.doe", scimGroup.members!![0].display)
        assertEquals("User", scimGroup.members!![0].type)
        assertEquals("$baseUrl/scim/v2/Users/$userId", scimGroup.members!![0].ref)
    }

    @Test
    fun `toDomain creates Group from ScimGroup`() {
        val scimGroup =
            ScimGroup(
                displayName = "Engineers",
                externalId = "ext-eng-001",
            )

        val group = scimGroup.toDomain()

        assertEquals("Engineers", group.displayName)
        assertEquals("ext-eng-001", group.externalId)
        assertNotNull(group.id)
    }

    @Test
    fun `toDomain preserves id if provided`() {
        val existingId = UUID.randomUUID()
        val scimGroup =
            ScimGroup(
                id = existingId.toString(),
                displayName = "Test Group",
            )

        val group = scimGroup.toDomain()

        assertEquals(existingId, group.id)
    }

    @Test
    fun `updateDomain updates existing group`() {
        val existingGroup =
            Group(
                id = UUID.randomUUID(),
                displayName = "Old Name",
                externalId = "old-ext",
            )

        val scimGroup =
            ScimGroup(
                displayName = "New Name",
                externalId = "new-ext",
            )

        val updatedGroup = scimGroup.updateDomain(existingGroup)

        assertEquals(existingGroup.id, updatedGroup.id)
        assertEquals("New Name", updatedGroup.displayName)
        assertEquals("new-ext", updatedGroup.externalId)
    }

    @Test
    fun `toGroupMember creates GroupMember from ScimMember`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()

        val scimMember =
            ScimMember(
                value = memberId.toString(),
                type = "User",
            )

        val groupMember = scimMember.toGroupMember(groupId)

        assertEquals(groupId, groupMember.groupId)
        assertEquals(memberId, groupMember.memberId)
        assertEquals(MemberType.USER, groupMember.memberType)
    }

    @Test
    fun `toGroupMember handles Group type`() {
        val groupId = UUID.randomUUID()
        val nestedGroupId = UUID.randomUUID()

        val scimMember =
            ScimMember(
                value = nestedGroupId.toString(),
                type = "Group",
            )

        val groupMember = scimMember.toGroupMember(groupId)

        assertEquals(MemberType.GROUP, groupMember.memberType)
    }
}

package com.revethq.iam.scim.api

import com.revethq.core.Metadata
import com.revethq.iam.scim.dtos.ScimGroup
import com.revethq.iam.scim.dtos.ScimMember
import com.revethq.iam.scim.dtos.ScimPatchOp
import com.revethq.iam.scim.dtos.ScimPatchOperation
import com.revethq.iam.scim.exception.ScimConflictException
import com.revethq.iam.scim.exception.ScimNotFoundException
import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.domain.MemberType
import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.Page
import com.revethq.iam.user.persistence.service.GroupService
import com.revethq.iam.user.persistence.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.ws.rs.core.UriInfo
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GroupResourceTest {

    private val groupService = mockk<GroupService>(relaxed = true)
    private val userService = mockk<UserService>(relaxed = true)
    private val uriInfo = mockk<UriInfo>().apply {
        every { baseUri } returns URI.create("https://example.com/")
    }

    private val groupResource = GroupResource().apply {
        this.groupService = this@GroupResourceTest.groupService
        this.userService = this@GroupResourceTest.userService
        this.uriInfo = this@GroupResourceTest.uriInfo
    }

    @Test
    fun `createGroup returns 201 with created group`() {
        val scimGroup = ScimGroup(displayName = "New Group")
        every { groupService.findByDisplayName("New Group") } returns null
        every { groupService.create(any()) } answers {
            firstArg<Group>().copy(
                createdOn = OffsetDateTime.now(),
                updatedOn = OffsetDateTime.now()
            )
        }

        val response = groupResource.createGroup(scimGroup)

        assertEquals(201, response.status)
        assertNotNull(response.entity)
        verify { groupService.create(any()) }
    }

    @Test
    fun `createGroup throws conflict when displayName exists`() {
        val scimGroup = ScimGroup(displayName = "Existing Group")
        every { groupService.findByDisplayName("Existing Group") } returns createGroup("Existing Group")

        assertFailsWith<ScimConflictException> {
            groupResource.createGroup(scimGroup)
        }
    }

    @Test
    fun `createGroup throws conflict when externalId exists`() {
        val scimGroup = ScimGroup(displayName = "New Group", externalId = "existing-ext")
        every { groupService.findByDisplayName("New Group") } returns null
        every { groupService.findByExternalId("existing-ext") } returns createGroup("Other")

        assertFailsWith<ScimConflictException> {
            groupResource.createGroup(scimGroup)
        }
    }

    @Test
    fun `createGroup with members adds members`() {
        val memberId = UUID.randomUUID()
        val scimGroup = ScimGroup(
            displayName = "Group with Members",
            members = listOf(ScimMember(value = memberId.toString(), type = "User"))
        )
        every { groupService.findByDisplayName(any()) } returns null
        every { groupService.create(any()) } answers {
            firstArg<Group>().copy(
                createdOn = OffsetDateTime.now(),
                updatedOn = OffsetDateTime.now()
            )
        }
        every { groupService.addMember(any(), any()) } answers {
            secondArg<GroupMember>().copy(id = UUID.randomUUID())
        }

        val response = groupResource.createGroup(scimGroup)

        assertEquals(201, response.status)
        verify { groupService.addMember(any(), any()) }
    }

    @Test
    fun `listGroups returns paginated results`() {
        val groups = listOf(
            createGroup("Group1"),
            createGroup("Group2")
        )
        every { groupService.list(any(), any()) } returns Page(groups, 2L, 0, 100)
        every { groupService.getMembers(any()) } returns emptyList()

        val result = groupResource.listGroups(null, 1, 100)

        assertEquals(2, result.totalResults)
        assertEquals(2, result.resources.size)
        assertEquals(1, result.startIndex)
    }

    @Test
    fun `getGroup returns group with members`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val group = createGroup("Test Group", groupId)
        val members = listOf(
            GroupMember(id = UUID.randomUUID(), groupId = groupId, memberId = memberId, memberType = MemberType.USER)
        )
        val user = User(id = memberId, username = "memberuser", email = "member@example.com")

        every { groupService.findById(groupId) } returns group
        every { groupService.getMembers(groupId) } returns members
        every { userService.findById(memberId) } returns user

        val result = groupResource.getGroup(groupId.toString())

        assertEquals("Test Group", result.displayName)
        assertNotNull(result.members)
        assertEquals(1, result.members!!.size)
        assertEquals(memberId.toString(), result.members!![0].value)
    }

    @Test
    fun `getGroup throws not found when group missing`() {
        val groupId = UUID.randomUUID()
        every { groupService.findById(groupId) } returns null

        assertFailsWith<ScimNotFoundException> {
            groupResource.getGroup(groupId.toString())
        }
    }

    @Test
    fun `replaceGroup updates group and replaces members`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val existingGroup = createGroup("Old Name", groupId)
        val scimGroup = ScimGroup(
            displayName = "New Name",
            members = listOf(ScimMember(value = memberId.toString(), type = "User"))
        )

        every { groupService.findById(groupId) } returns existingGroup
        every { groupService.update(any()) } answers { firstArg() }
        every { groupService.setMembers(any(), any()) } answers {
            secondArg<List<GroupMember>>().map { it.copy(id = UUID.randomUUID()) }
        }
        every { groupService.getMembers(groupId) } returns emptyList()

        val result = groupResource.replaceGroup(groupId.toString(), scimGroup)

        assertEquals("New Name", result.displayName)
        verify { groupService.update(any()) }
        verify { groupService.setMembers(groupId, any()) }
    }

    @Test
    fun `replaceGroup throws not found when group missing`() {
        val groupId = UUID.randomUUID()
        every { groupService.findById(groupId) } returns null

        assertFailsWith<ScimNotFoundException> {
            groupResource.replaceGroup(groupId.toString(), ScimGroup(displayName = "test"))
        }
    }

    @Test
    fun `patchGroup updates displayName`() {
        val groupId = UUID.randomUUID()
        val existingGroup = createGroup("Old Name", groupId)
        val updatedGroup = createGroup("New Name", groupId)
        val patchOp = ScimPatchOp(
            operations = listOf(
                ScimPatchOperation(op = "replace", path = "displayName", value = "New Name")
            )
        )

        // First call returns existing, second call (after update) returns updated
        every { groupService.findById(groupId) } returnsMany listOf(existingGroup, updatedGroup)
        every { groupService.update(any()) } answers { firstArg() }
        every { groupService.getMembers(groupId) } returns emptyList()

        val result = groupResource.patchGroup(groupId.toString(), patchOp)

        assertEquals("New Name", result.displayName)
    }

    @Test
    fun `patchGroup adds members`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val existingGroup = createGroup("Test Group", groupId)
        val patchOp = ScimPatchOp(
            operations = listOf(
                ScimPatchOperation(
                    op = "add",
                    path = "members",
                    value = listOf(mapOf("value" to memberId.toString()))
                )
            )
        )

        every { groupService.findById(groupId) } returns existingGroup
        every { groupService.addMember(any(), any()) } answers {
            secondArg<GroupMember>().copy(id = UUID.randomUUID())
        }
        every { groupService.getMembers(groupId) } returns emptyList()

        groupResource.patchGroup(groupId.toString(), patchOp)

        verify { groupService.addMember(groupId, any()) }
    }

    @Test
    fun `patchGroup removes member`() {
        val groupId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val existingGroup = createGroup("Test Group", groupId)
        val patchOp = ScimPatchOp(
            operations = listOf(
                ScimPatchOperation(
                    op = "remove",
                    path = "members[value eq \"$memberId\"]"
                )
            )
        )

        every { groupService.findById(groupId) } returns existingGroup
        every { groupService.removeMember(groupId, memberId) } returns true
        every { groupService.getMembers(groupId) } returns emptyList()

        groupResource.patchGroup(groupId.toString(), patchOp)

        verify { groupService.removeMember(groupId, memberId) }
    }

    @Test
    fun `deleteGroup returns 204 on success`() {
        val groupId = UUID.randomUUID()
        every { groupService.delete(groupId) } returns true

        val response = groupResource.deleteGroup(groupId.toString())

        assertEquals(204, response.status)
    }

    @Test
    fun `deleteGroup throws not found when group missing`() {
        val groupId = UUID.randomUUID()
        every { groupService.delete(groupId) } returns false

        assertFailsWith<ScimNotFoundException> {
            groupResource.deleteGroup(groupId.toString())
        }
    }

    private fun createGroup(displayName: String, id: UUID = UUID.randomUUID()): Group {
        return Group(
            id = id,
            displayName = displayName,
            metadata = Metadata(),
            createdOn = OffsetDateTime.now(),
            updatedOn = OffsetDateTime.now()
        )
    }
}

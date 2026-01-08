package com.revethq.iam.scim.api

import com.revethq.core.Metadata
import com.revethq.iam.scim.dtos.ScimEmail
import com.revethq.iam.scim.dtos.ScimPatchOp
import com.revethq.iam.scim.dtos.ScimPatchOperation
import com.revethq.iam.scim.dtos.ScimUser
import com.revethq.iam.scim.exception.ScimConflictException
import com.revethq.iam.scim.exception.ScimNotFoundException
import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.Page
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

class UserResourceTest {

    private val userService = mockk<UserService>(relaxed = true)
    private val scimRequestContext = ScimRequestContext().apply {
        identityProviderId = UUID.randomUUID()
    }
    private val uriInfo = mockk<UriInfo>().apply {
        every { baseUri } returns URI.create("https://example.com/")
    }

    private val userResource = UserResource().apply {
        this.userService = this@UserResourceTest.userService
        this.scimRequestContext = this@UserResourceTest.scimRequestContext
        this.uriInfo = this@UserResourceTest.uriInfo
    }

    @Test
    fun `createUser returns 201 with created user`() {
        val scimUser = ScimUser(
            userName = "newuser",
            emails = listOf(ScimEmail(value = "new@example.com", primary = true))
        )
        every { userService.findByUsername("newuser") } returns null
        every { userService.create(any(), any(), any()) } answers {
            firstArg<User>().copy(
                createdOn = OffsetDateTime.now(),
                updatedOn = OffsetDateTime.now()
            )
        }

        val response = userResource.createUser(scimUser)

        assertEquals(201, response.status)
        assertNotNull(response.entity)
        verify { userService.create(any(), scimRequestContext.identityProviderId!!, null) }
    }

    @Test
    fun `createUser throws conflict when username exists`() {
        val scimUser = ScimUser(
            userName = "existing",
            emails = listOf(ScimEmail(value = "test@example.com", primary = true))
        )
        every { userService.findByUsername("existing") } returns createUser("existing")

        assertFailsWith<ScimConflictException> {
            userResource.createUser(scimUser)
        }
    }

    @Test
    fun `createUser throws conflict when externalId exists`() {
        val scimUser = ScimUser(
            userName = "newuser",
            externalId = "existing-ext-id",
            emails = listOf(ScimEmail(value = "test@example.com", primary = true))
        )
        every { userService.findByUsername("newuser") } returns null
        every { userService.findByExternalId("existing-ext-id", any()) } returns createUser("other")

        assertFailsWith<ScimConflictException> {
            userResource.createUser(scimUser)
        }
    }

    @Test
    fun `listUsers returns paginated results`() {
        val users = listOf(
            createUser("user1"),
            createUser("user2")
        )
        every { userService.list(any(), any()) } returns Page(users, 2L, 0, 100)
        every { userService.getExternalId(any(), any()) } returns null

        val result = userResource.listUsers(null, 1, 100)

        assertEquals(2, result.totalResults)
        assertEquals(2, result.resources.size)
        assertEquals(1, result.startIndex)
    }

    @Test
    fun `getUser returns user when found`() {
        val userId = UUID.randomUUID()
        val user = createUser("testuser", userId)
        every { userService.findById(userId) } returns user
        every { userService.getExternalId(userId, any()) } returns "ext-123"

        val result = userResource.getUser(userId.toString())

        assertEquals("testuser", result.userName)
        assertEquals("ext-123", result.externalId)
    }

    @Test
    fun `getUser throws not found when user missing`() {
        val userId = UUID.randomUUID()
        every { userService.findById(userId) } returns null

        assertFailsWith<ScimNotFoundException> {
            userResource.getUser(userId.toString())
        }
    }

    @Test
    fun `replaceUser updates existing user`() {
        val userId = UUID.randomUUID()
        val existingUser = createUser("oldname", userId)
        val scimUser = ScimUser(
            userName = "newname",
            emails = listOf(ScimEmail(value = "new@example.com", primary = true))
        )

        every { userService.findById(userId) } returns existingUser
        every { userService.update(any()) } answers { firstArg() }

        val result = userResource.replaceUser(userId.toString(), scimUser)

        assertEquals("newname", result.userName)
        verify { userService.update(any()) }
        verify { userService.updateExternalId(userId, any(), null) }
    }

    @Test
    fun `replaceUser throws not found when user missing`() {
        val userId = UUID.randomUUID()
        every { userService.findById(userId) } returns null

        assertFailsWith<ScimNotFoundException> {
            userResource.replaceUser(userId.toString(), ScimUser(userName = "test"))
        }
    }

    @Test
    fun `patchUser updates username`() {
        val userId = UUID.randomUUID()
        val existingUser = createUser("oldname", userId)
        val patchOp = ScimPatchOp(
            operations = listOf(
                ScimPatchOperation(op = "replace", path = "userName", value = "newname")
            )
        )

        every { userService.findById(userId) } returns existingUser
        every { userService.getExternalId(userId, any()) } returns null
        every { userService.update(any()) } answers { firstArg() }

        val result = userResource.patchUser(userId.toString(), patchOp)

        assertEquals("newname", result.userName)
    }

    @Test
    fun `patchUser updates active status`() {
        val userId = UUID.randomUUID()
        val existingUser = createUser("testuser", userId)
        val patchOp = ScimPatchOp(
            operations = listOf(
                ScimPatchOperation(op = "replace", path = "active", value = false)
            )
        )

        every { userService.findById(userId) } returns existingUser
        every { userService.getExternalId(userId, any()) } returns null
        every { userService.update(any()) } answers { firstArg() }

        val result = userResource.patchUser(userId.toString(), patchOp)

        assertEquals(false, result.active)
    }

    @Test
    fun `deleteUser returns 204 on success`() {
        val userId = UUID.randomUUID()
        every { userService.delete(userId) } returns true

        val response = userResource.deleteUser(userId.toString())

        assertEquals(204, response.status)
    }

    @Test
    fun `deleteUser throws not found when user missing`() {
        val userId = UUID.randomUUID()
        every { userService.delete(userId) } returns false

        assertFailsWith<ScimNotFoundException> {
            userResource.deleteUser(userId.toString())
        }
    }

    private fun createUser(username: String, id: UUID = UUID.randomUUID()): User {
        return User(
            id = id,
            username = username,
            email = "$username@example.com",
            metadata = Metadata(properties = mapOf("active" to true)),
            createdOn = OffsetDateTime.now(),
            updatedOn = OffsetDateTime.now()
        )
    }
}

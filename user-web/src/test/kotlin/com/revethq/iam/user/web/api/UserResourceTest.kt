package com.revethq.iam.user.web.api

import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.Page
import com.revethq.iam.user.persistence.service.UserService
import com.revethq.iam.user.web.dto.CreateUserRequest
import com.revethq.iam.user.web.dto.UpdateUserRequest
import com.revethq.iam.user.web.exception.UserConflictException
import com.revethq.iam.user.web.exception.UserNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserResourceTest {

    private val userService = mockk<UserService>(relaxed = true)

    private val resource = UserResource().apply {
        this.userService = this@UserResourceTest.userService
    }

    @Test
    fun `createUser returns 201 with created user`() {
        val request = CreateUserRequest(
            username = "alice",
            email = "alice@example.com"
        )

        every { userService.findByUsername("alice") } returns null
        every { userService.findByEmail("alice@example.com") } returns null
        val userSlot = slot<User>()
        every { userService.create(capture(userSlot), any(), any()) } answers { userSlot.captured }

        val response = resource.createUser(request, null)

        assertEquals(201, response.status)
        verify { userService.create(any(), any(), any()) }
    }

    @Test
    fun `createUser throws conflict when username exists`() {
        val existingUser = createUser("alice")

        every { userService.findByUsername("alice") } returns existingUser

        val request = CreateUserRequest(
            username = "alice",
            email = "alice2@example.com"
        )

        assertFailsWith<UserConflictException> {
            resource.createUser(request, null)
        }
    }

    @Test
    fun `createUser throws conflict when email exists`() {
        every { userService.findByUsername("bob") } returns null
        every { userService.findByEmail("alice@example.com") } returns createUser("alice")

        val request = CreateUserRequest(
            username = "bob",
            email = "alice@example.com"
        )

        assertFailsWith<UserConflictException> {
            resource.createUser(request, null)
        }
    }

    @Test
    fun `getUser returns user when found`() {
        val user = createUser("alice")
        every { userService.findById(user.id) } returns user

        val result = resource.getUser(user.id.toString())

        assertEquals(user.username, result.username)
        assertEquals(user.email, result.email)
    }

    @Test
    fun `getUser throws not found when missing`() {
        val id = UUID.randomUUID()
        every { userService.findById(id) } returns null

        assertFailsWith<UserNotFoundException> {
            resource.getUser(id.toString())
        }
    }

    @Test
    fun `listUsers returns paginated results`() {
        val users = listOf(createUser("alice"), createUser("bob"))
        val page = Page(users, 2, 0, 20)

        every { userService.list(0, 21) } returns page

        val result = resource.listUsers(0, 20)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertFalse(result.hasMore)
    }

    @Test
    fun `listUsers indicates hasMore when more results exist`() {
        val users = (1..21).map { createUser("user$it") }
        val page = Page(users, 21, 0, 21)

        every { userService.list(0, 21) } returns page

        val result = resource.listUsers(0, 20)

        assertEquals(20, result.content.size)
        assertTrue(result.hasMore)
    }

    @Test
    fun `updateUser updates existing user`() {
        val existing = createUser("alice")
        every { userService.findById(existing.id) } returns existing

        val request = UpdateUserRequest(
            username = "alice-updated",
            email = "alice-updated@example.com"
        )

        val userSlot = slot<User>()
        every { userService.update(capture(userSlot)) } answers { userSlot.captured }

        val result = resource.updateUser(existing.id.toString(), request)

        assertEquals("alice-updated", result.username)
        assertEquals("alice-updated@example.com", result.email)
    }

    @Test
    fun `updateUser throws not found when missing`() {
        val id = UUID.randomUUID()
        every { userService.findById(id) } returns null

        val request = UpdateUserRequest(
            username = "alice",
            email = "alice@example.com"
        )

        assertFailsWith<UserNotFoundException> {
            resource.updateUser(id.toString(), request)
        }
    }

    @Test
    fun `deleteUser returns 204 on success`() {
        val id = UUID.randomUUID()
        every { userService.delete(id) } returns true

        val response = resource.deleteUser(id.toString())

        assertEquals(204, response.status)
    }

    @Test
    fun `deleteUser throws not found when missing`() {
        val id = UUID.randomUUID()
        every { userService.delete(id) } returns false

        assertFailsWith<UserNotFoundException> {
            resource.deleteUser(id.toString())
        }
    }

    private fun createUser(username: String, id: UUID = UUID.randomUUID()): User = User(
        id = id,
        username = username,
        email = "$username@example.com"
    )
}

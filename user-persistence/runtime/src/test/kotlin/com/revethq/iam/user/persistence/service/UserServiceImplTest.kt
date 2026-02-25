package com.revethq.iam.user.persistence.service

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.entity.IdentityProviderLinkEntity
import com.revethq.iam.user.persistence.entity.UserEntity
import com.revethq.iam.user.persistence.repository.IdentityProviderLinkRepository
import com.revethq.iam.user.persistence.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserServiceImplTest {
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val identityProviderLinkRepository = mockk<IdentityProviderLinkRepository>(relaxed = true)
    private val userService = UserServiceImpl(userRepository, identityProviderLinkRepository)

    @Test
    fun `create persists user and returns domain object`() {
        val user =
            User(
                id = UUID.randomUUID(),
                username = "testuser",
                email = "test@example.com",
            )
        val identityProviderId = UUID.randomUUID()

        val result = userService.create(user, identityProviderId, null)

        verify { userRepository.persist(any<UserEntity>()) }
        assertEquals(user.username, result.username)
        assertEquals(user.email, result.email)
    }

    @Test
    fun `create with externalId creates identity provider link`() {
        val user =
            User(
                id = UUID.randomUUID(),
                username = "testuser",
                email = "test@example.com",
            )
        val identityProviderId = UUID.randomUUID()
        val externalId = "ext-123"

        userService.create(user, identityProviderId, externalId)

        verify { userRepository.persist(any<UserEntity>()) }
        verify { identityProviderLinkRepository.persist(any<IdentityProviderLinkEntity>()) }
    }

    @Test
    fun `create without externalId does not create link`() {
        val user =
            User(
                id = UUID.randomUUID(),
                username = "testuser",
                email = "test@example.com",
            )
        val identityProviderId = UUID.randomUUID()

        userService.create(user, identityProviderId, null)

        verify { userRepository.persist(any<UserEntity>()) }
        verify(exactly = 0) { identityProviderLinkRepository.persist(any<IdentityProviderLinkEntity>()) }
    }

    @Test
    fun `findById returns user when found`() {
        val userId = UUID.randomUUID()
        val entity = createUserEntity(userId, "founduser", "found@example.com")
        every { userRepository.findById(userId) } returns entity

        val result = userService.findById(userId)

        assertNotNull(result)
        assertEquals("founduser", result.username)
    }

    @Test
    fun `findById returns null when not found`() {
        val userId = UUID.randomUUID()
        every { userRepository.findById(userId) } returns null

        val result = userService.findById(userId)

        assertNull(result)
    }

    @Test
    fun `findByUsername returns user when found`() {
        val entity = createUserEntity(UUID.randomUUID(), "johndoe", "john@example.com")
        every { userRepository.findByUsername("johndoe") } returns entity

        val result = userService.findByUsername("johndoe")

        assertNotNull(result)
        assertEquals("johndoe", result.username)
    }

    @Test
    fun `findByUsername returns null when not found`() {
        every { userRepository.findByUsername("unknown") } returns null

        val result = userService.findByUsername("unknown")

        assertNull(result)
    }

    @Test
    fun `findByEmail returns user when found`() {
        val entity = createUserEntity(UUID.randomUUID(), "jane", "jane@example.com")
        every { userRepository.findByEmail("jane@example.com") } returns entity

        val result = userService.findByEmail("jane@example.com")

        assertNotNull(result)
        assertEquals("jane@example.com", result.email)
    }

    @Test
    fun `findByExternalId returns user when link exists`() {
        val userId = UUID.randomUUID()
        val identityProviderId = UUID.randomUUID()
        val linkEntity = createLinkEntity(userId, identityProviderId, "ext-456")
        val userEntity = createUserEntity(userId, "linkeduser", "linked@example.com")

        every { identityProviderLinkRepository.findByExternalIdAndIdentityProviderId("ext-456", identityProviderId) } returns linkEntity
        every { userRepository.findById(userId) } returns userEntity

        val result = userService.findByExternalId("ext-456", identityProviderId)

        assertNotNull(result)
        assertEquals("linkeduser", result.username)
    }

    @Test
    fun `findByExternalId returns null when link not found`() {
        val identityProviderId = UUID.randomUUID()
        every { identityProviderLinkRepository.findByExternalIdAndIdentityProviderId("unknown", identityProviderId) } returns null

        val result = userService.findByExternalId("unknown", identityProviderId)

        assertNull(result)
    }

    @Test
    fun `getExternalId returns externalId when link exists`() {
        val userId = UUID.randomUUID()
        val identityProviderId = UUID.randomUUID()
        val linkEntity = createLinkEntity(userId, identityProviderId, "ext-789")

        every { identityProviderLinkRepository.findByUserIdAndIdentityProviderId(userId, identityProviderId) } returns linkEntity

        val result = userService.getExternalId(userId, identityProviderId)

        assertEquals("ext-789", result)
    }

    @Test
    fun `getExternalId returns null when link not found`() {
        val userId = UUID.randomUUID()
        val identityProviderId = UUID.randomUUID()
        every { identityProviderLinkRepository.findByUserIdAndIdentityProviderId(userId, identityProviderId) } returns null

        val result = userService.getExternalId(userId, identityProviderId)

        assertNull(result)
    }

    @Test
    fun `update modifies existing user`() {
        val userId = UUID.randomUUID()
        val existingEntity = createUserEntity(userId, "oldname", "old@example.com")
        every { userRepository.findById(userId) } returns existingEntity

        val updatedUser =
            User(
                id = userId,
                username = "newname",
                email = "new@example.com",
                metadata = Metadata(properties = mapOf("key" to "value")),
            )

        val result = userService.update(updatedUser)

        assertEquals("newname", existingEntity.username)
        assertEquals("new@example.com", existingEntity.email)
        assertNotNull(result)
    }

    @Test
    fun `delete returns true when user deleted`() {
        val userId = UUID.randomUUID()
        every { userRepository.deleteById(userId) } returns true

        val result = userService.delete(userId)

        assertTrue(result)
    }

    @Test
    fun `count returns repository count`() {
        every { userRepository.count() } returns 42L

        val result = userService.count()

        assertEquals(42L, result)
    }

    private fun createUserEntity(
        id: UUID,
        username: String,
        email: String,
    ): UserEntity =
        UserEntity().apply {
            this.id = id
            this.username = username
            this.email = email
            this.metadata = Metadata()
            this.createdOn = OffsetDateTime.now()
            this.updatedOn = OffsetDateTime.now()
        }

    private fun createLinkEntity(
        userId: UUID,
        identityProviderId: UUID,
        externalId: String,
    ): IdentityProviderLinkEntity =
        IdentityProviderLinkEntity().apply {
            this.id = UUID.randomUUID()
            this.userId = userId
            this.identityProviderId = identityProviderId
            this.externalId = externalId
            this.metadata = Metadata()
            this.createdOn = OffsetDateTime.now()
            this.updatedOn = OffsetDateTime.now()
        }
}

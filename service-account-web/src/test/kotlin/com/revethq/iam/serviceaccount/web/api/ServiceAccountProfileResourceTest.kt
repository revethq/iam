package com.revethq.iam.serviceaccount.web.api

import com.revethq.iam.serviceaccount.domain.ServiceAccount
import com.revethq.iam.serviceaccount.persistence.service.ServiceAccountService
import com.revethq.iam.serviceaccount.web.exception.ServiceAccountNotFoundException
import com.revethq.iam.user.domain.ProfileType
import com.revethq.iam.user.persistence.entity.ProfileEntity
import com.revethq.iam.user.persistence.repository.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ServiceAccountProfileResourceTest {

    private val serviceAccountService = mockk<ServiceAccountService>()
    private val profileRepository = mockk<ProfileRepository>(relaxed = true)

    private val resource = ServiceAccountProfileResource().apply {
        this.serviceAccountService = this@ServiceAccountProfileResourceTest.serviceAccountService
        this.profileRepository = this@ServiceAccountProfileResourceTest.profileRepository
    }

    @Test
    fun `getProfile returns profile when found`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot")
        every { serviceAccountService.findById(id) } returns sa

        val profileEntity = ProfileEntity().apply {
            this.id = UUID.randomUUID()
            resource = id
            profileType = ProfileType.ServiceAccount
            profile = mapOf("role" to "worker")
            createdOn = OffsetDateTime.now()
            updatedOn = OffsetDateTime.now()
        }
        every { profileRepository.findByResourceAndProfileType(id, ProfileType.ServiceAccount) } returns profileEntity

        val response = resource.getProfile(id.toString())

        assertEquals(200, response.status)
    }

    @Test
    fun `getProfile returns 404 when profile not found`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot")
        every { serviceAccountService.findById(id) } returns sa
        every { profileRepository.findByResourceAndProfileType(id, ProfileType.ServiceAccount) } returns null

        val response = resource.getProfile(id.toString())

        assertEquals(404, response.status)
    }

    @Test
    fun `getProfile throws not found when service account missing`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.findById(id) } returns null

        assertFailsWith<ServiceAccountNotFoundException> {
            resource.getProfile(id.toString())
        }
    }

    @Test
    fun `setProfile creates new profile`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot")
        every { serviceAccountService.findById(id) } returns sa
        every { profileRepository.findByResourceAndProfileType(id, ProfileType.ServiceAccount) } returns null

        val profileData = mapOf("role" to "worker", "env" to "prod")
        val response = resource.setProfile(id.toString(), profileData)

        assertEquals(201, response.status)
        verify { profileRepository.persist(any<ProfileEntity>()) }
    }

    @Test
    fun `setProfile updates existing profile`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot")
        every { serviceAccountService.findById(id) } returns sa

        val existingEntity = ProfileEntity().apply {
            this.id = UUID.randomUUID()
            resource = id
            profileType = ProfileType.ServiceAccount
            profile = mapOf("role" to "old")
            createdOn = OffsetDateTime.now()
            updatedOn = OffsetDateTime.now()
        }
        every { profileRepository.findByResourceAndProfileType(id, ProfileType.ServiceAccount) } returns existingEntity

        val profileData = mapOf("role" to "updated")
        val response = resource.setProfile(id.toString(), profileData)

        assertEquals(200, response.status)
        assertEquals(profileData, existingEntity.profile)
    }

    @Test
    fun `setProfile throws not found when service account missing`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.findById(id) } returns null

        assertFailsWith<ServiceAccountNotFoundException> {
            resource.setProfile(id.toString(), mapOf("key" to "value"))
        }
    }
}

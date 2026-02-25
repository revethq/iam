package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.persistence.entity.PolicyEntity
import com.revethq.iam.permission.persistence.repository.PolicyAttachmentRepository
import com.revethq.iam.permission.persistence.repository.PolicyRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PolicyServiceImplTest {
    private val policyRepository = mockk<PolicyRepository>(relaxed = true)
    private val policyAttachmentRepository = mockk<PolicyAttachmentRepository>(relaxed = true)
    private val service = PolicyServiceImpl(policyRepository, policyAttachmentRepository)

    @Test
    fun `create persists policy and returns domain object`() {
        val policy = createPolicy("TestPolicy")
        val entitySlot = slot<PolicyEntity>()

        every { policyRepository.persist(capture(entitySlot)) } just runs

        val result = service.create(policy)

        verify { policyRepository.persist(any<PolicyEntity>()) }
        assertEquals(policy.id, result.id)
        assertEquals(policy.name, result.name)
    }

    @Test
    fun `findById returns policy when found`() {
        val policy = createPolicy("TestPolicy")
        val entity = PolicyEntity.fromDomain(policy)

        every { policyRepository.findById(policy.id) } returns entity

        val result = service.findById(policy.id)

        assertNotNull(result)
        assertEquals(policy.id, result.id)
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { policyRepository.findById(id) } returns null

        val result = service.findById(id)

        assertNull(result)
    }

    @Test
    fun `findByName returns policy when found`() {
        val policy = createPolicy("TestPolicy")
        val entity = PolicyEntity.fromDomain(policy)

        every { policyRepository.findByNameAndTenantId("TestPolicy", null) } returns entity

        val result = service.findByName("TestPolicy")

        assertNotNull(result)
        assertEquals("TestPolicy", result.name)
    }

    @Test
    fun `update modifies existing policy`() {
        val originalPolicy = createPolicy("OriginalName")
        val originalEntity = PolicyEntity.fromDomain(originalPolicy)
        val updatedPolicy = originalPolicy.copy(name = "UpdatedName")

        every { policyRepository.findById(originalPolicy.id) } returns originalEntity

        val result = service.update(updatedPolicy)

        assertEquals("UpdatedName", result.name)
    }

    @Test
    fun `delete returns true when policy deleted`() {
        val id = UUID.randomUUID()

        every { policyAttachmentRepository.deleteByPolicyId(id) } returns 0
        every { policyRepository.deleteById(id) } returns true

        val result = service.delete(id)

        assertTrue(result)
    }

    @Test
    fun `delete returns false when policy not found`() {
        val id = UUID.randomUUID()

        every { policyAttachmentRepository.deleteByPolicyId(id) } returns 0
        every { policyRepository.deleteById(id) } returns false

        val result = service.delete(id)

        assertFalse(result)
    }

    private fun createPolicy(name: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = name,
            version = "2026-01-15",
            statements =
                listOf(
                    Statement(
                        effect = Effect.ALLOW,
                        actions = listOf("iam:*"),
                        resources = listOf("urn:revet:iam:*:user/*"),
                    ),
                ),
        )
}

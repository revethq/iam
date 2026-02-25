package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.persistence.entity.PolicyAttachmentEntity
import com.revethq.iam.permission.persistence.entity.PolicyEntity
import com.revethq.iam.permission.persistence.repository.PolicyAttachmentRepository
import com.revethq.iam.permission.persistence.repository.PolicyRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyAttachmentServiceImplTest {
    private val policyAttachmentRepository = mockk<PolicyAttachmentRepository>(relaxed = true)
    private val policyRepository = mockk<PolicyRepository>(relaxed = true)
    private val service = PolicyAttachmentServiceImpl(policyAttachmentRepository, policyRepository)

    private val policyId = UUID.randomUUID()
    private val principalUrn = "urn:revet:iam::user/alice"

    @Test
    fun `attach policy to principal`() {
        every { policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn) } returns null
        every { policyAttachmentRepository.persist(any<PolicyAttachmentEntity>()) } just runs

        val result = service.attach(policyId, principalUrn, "admin")

        assertEquals(policyId, result.policyId)
        assertEquals(principalUrn, result.principalUrn)
        verify { policyAttachmentRepository.persist(any<PolicyAttachmentEntity>()) }
    }

    @Test
    fun `attach same policy twice fails`() {
        val existingEntity = createAttachmentEntity()
        every { policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn) } returns existingEntity

        assertFailsWith<IllegalStateException> {
            service.attach(policyId, principalUrn, null)
        }
    }

    @Test
    fun `detach policy from principal`() {
        val attachmentId = UUID.randomUUID()
        val attachment = createAttachmentEntity(policyId = policyId).apply { id = attachmentId }
        every { policyAttachmentRepository.findById(attachmentId) } returns attachment
        every { policyAttachmentRepository.deleteById(attachmentId) } returns true

        val result = service.detach(policyId, attachmentId)

        assertTrue(result)
    }

    @Test
    fun `detach returns false when attachment not found`() {
        val attachmentId = UUID.randomUUID()
        every { policyAttachmentRepository.findById(attachmentId) } returns null

        val result = service.detach(policyId, attachmentId)

        assertFalse(result)
    }

    @Test
    fun `detach returns false when attachment belongs to different policy`() {
        val attachmentId = UUID.randomUUID()
        val differentPolicyId = UUID.randomUUID()
        val attachment = createAttachmentEntity(policyId = differentPolicyId).apply { id = attachmentId }
        every { policyAttachmentRepository.findById(attachmentId) } returns attachment

        val result = service.detach(policyId, attachmentId)

        assertFalse(result)
    }

    @Test
    fun `list attachments for policy`() {
        val attachment1 = createAttachmentEntity(principalUrn = "urn:revet:iam::user/alice")
        val attachment2 = createAttachmentEntity(principalUrn = "urn:revet:iam::user/bob")

        every { policyAttachmentRepository.findByPolicyId(policyId) } returns listOf(attachment1, attachment2)

        val result = service.listAttachmentsForPolicy(policyId)

        assertEquals(2, result.size)
    }

    @Test
    fun `list policies for principal`() {
        val policy1Id = UUID.randomUUID()
        val attachment = createAttachmentEntity(policyId = policy1Id)
        val policyEntity = createPolicyEntity(policy1Id, "Policy1")

        every { policyAttachmentRepository.findByPrincipalUrn(principalUrn) } returns listOf(attachment)
        every { policyRepository.findById(policy1Id) } returns policyEntity

        val result = service.listPoliciesForPrincipal(principalUrn)

        assertEquals(1, result.size)
        assertEquals("Policy1", result[0].name)
    }

    @Test
    fun `isAttached returns true when attached`() {
        every { policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn) } returns createAttachmentEntity()

        assertTrue(service.isAttached(policyId, principalUrn))
    }

    @Test
    fun `isAttached returns false when not attached`() {
        every { policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn) } returns null

        assertFalse(service.isAttached(policyId, principalUrn))
    }

    private fun createAttachmentEntity(
        policyId: UUID = this.policyId,
        principalUrn: String = this.principalUrn,
    ) = PolicyAttachmentEntity().apply {
        id = UUID.randomUUID()
        this.policyId = policyId
        this.principalUrn = principalUrn
        attachedOn = OffsetDateTime.now()
    }

    private fun createPolicyEntity(
        id: UUID,
        name: String,
    ): PolicyEntity {
        val policy =
            Policy(
                id = id,
                name = name,
                version = "2026-01-15",
                statements =
                    listOf(
                        Statement(effect = Effect.ALLOW, actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*")),
                    ),
            )
        return PolicyEntity.fromDomain(policy)
    }
}

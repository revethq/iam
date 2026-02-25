package com.revethq.iam.permission.web.api

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.PolicyAttachment
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.persistence.Page
import com.revethq.iam.permission.service.PolicyAttachmentService
import com.revethq.iam.permission.persistence.service.PolicyService
import com.revethq.iam.permission.web.dto.AttachPolicyRequest
import com.revethq.iam.permission.web.dto.CreatePolicyRequest
import com.revethq.iam.permission.web.dto.StatementDto
import com.revethq.iam.permission.web.dto.UpdatePolicyRequest
import com.revethq.iam.permission.web.exception.PolicyAttachmentConflictException
import com.revethq.iam.permission.web.exception.PolicyAttachmentNotFoundException
import com.revethq.iam.permission.web.exception.PolicyConflictException
import com.revethq.iam.permission.web.exception.PolicyNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PolicyResourceTest {

    private val policyService = mockk<PolicyService>(relaxed = true)
    private val policyAttachmentService = mockk<PolicyAttachmentService>(relaxed = true)

    private val resource = PolicyResource().apply {
        this.policyService = this@PolicyResourceTest.policyService
        this.policyAttachmentService = this@PolicyResourceTest.policyAttachmentService
    }

    @Test
    fun `createPolicy returns 201 with created policy`() {
        val request = CreatePolicyRequest(
            name = "TestPolicy",
            version = "2026-01-15",
            statements = listOf(
                StatementDto(effect = "Allow", actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
            )
        )

        every { policyService.findByName("TestPolicy", null) } returns null
        val policySlot = slot<Policy>()
        every { policyService.create(capture(policySlot)) } answers { policySlot.captured }

        val response = resource.createPolicy(request)

        assertEquals(201, response.status)
        verify { policyService.create(any()) }
    }

    @Test
    fun `createPolicy throws conflict when name exists`() {
        val existingPolicy = createPolicy("TestPolicy")

        every { policyService.findByName("TestPolicy", null) } returns existingPolicy

        val request = CreatePolicyRequest(
            name = "TestPolicy",
            version = "2026-01-15",
            statements = listOf(
                StatementDto(effect = "Allow", actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
            )
        )

        assertFailsWith<PolicyConflictException> {
            resource.createPolicy(request)
        }
    }

    @Test
    fun `getPolicy returns policy when found`() {
        val policy = createPolicy("TestPolicy")
        every { policyService.findById(policy.id) } returns policy

        val result = resource.getPolicy(policy.id.toString())

        assertEquals(policy.name, result.name)
    }

    @Test
    fun `getPolicy throws not found when missing`() {
        val id = UUID.randomUUID()
        every { policyService.findById(id) } returns null

        assertFailsWith<PolicyNotFoundException> {
            resource.getPolicy(id.toString())
        }
    }

    @Test
    fun `listPolicies returns paginated results`() {
        val policy1 = createPolicy("Policy1")
        val policy2 = createPolicy("Policy2")
        val page = Page(listOf(policy1, policy2), 2, 0, 10)

        every { policyService.list(0, 100, null) } returns page

        val result = resource.listPolicies(0, 100, null)

        assertEquals(2, result.items.size)
        assertEquals(2, result.totalCount)
    }

    @Test
    fun `updatePolicy updates existing policy`() {
        val existing = createPolicy("OldName")
        every { policyService.findById(existing.id) } returns existing

        val request = UpdatePolicyRequest(
            name = "NewName",
            version = "2026-01-15",
            statements = listOf(
                StatementDto(effect = "Allow", actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
            )
        )

        val policySlot = slot<Policy>()
        every { policyService.update(capture(policySlot)) } answers { policySlot.captured }

        val result = resource.updatePolicy(existing.id.toString(), request)

        assertEquals("NewName", result.name)
    }

    @Test
    fun `updatePolicy throws not found when missing`() {
        val id = UUID.randomUUID()
        every { policyService.findById(id) } returns null

        val request = UpdatePolicyRequest(
            name = "NewName",
            version = "2026-01-15",
            statements = listOf(
                StatementDto(effect = "Allow", actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
            )
        )

        assertFailsWith<PolicyNotFoundException> {
            resource.updatePolicy(id.toString(), request)
        }
    }

    @Test
    fun `deletePolicy returns 204 on success`() {
        val id = UUID.randomUUID()
        every { policyService.delete(id) } returns true

        val response = resource.deletePolicy(id.toString())

        assertEquals(204, response.status)
    }

    @Test
    fun `deletePolicy throws not found when missing`() {
        val id = UUID.randomUUID()
        every { policyService.delete(id) } returns false

        assertFailsWith<PolicyNotFoundException> {
            resource.deletePolicy(id.toString())
        }
    }

    @Test
    fun `attachPolicy returns 201 on success`() {
        val policyId = UUID.randomUUID()
        val principalUrn = "urn:revet:iam::user/alice"
        val attachment = PolicyAttachment(UUID.randomUUID(), policyId, principalUrn)

        every { policyService.findById(policyId) } returns createPolicy("TestPolicy", policyId)
        every { policyAttachmentService.attach(policyId, principalUrn, null) } returns attachment

        val response = resource.attachPolicy(policyId.toString(), AttachPolicyRequest(principalUrn))

        assertEquals(201, response.status)
    }

    @Test
    fun `attachPolicy throws conflict when already attached`() {
        val policyId = UUID.randomUUID()
        val principalUrn = "urn:revet:iam::user/alice"

        every { policyService.findById(policyId) } returns createPolicy("TestPolicy", policyId)
        every { policyAttachmentService.attach(policyId, principalUrn, null) } throws
            IllegalStateException("Already attached")

        assertFailsWith<PolicyAttachmentConflictException> {
            resource.attachPolicy(policyId.toString(), AttachPolicyRequest(principalUrn))
        }
    }

    @Test
    fun `detachPolicy returns 204 on success`() {
        val policyId = UUID.randomUUID()
        val attachmentId = UUID.randomUUID()

        every { policyAttachmentService.detach(policyId, attachmentId) } returns true

        val response = resource.detachPolicy(policyId.toString(), attachmentId.toString())

        assertEquals(204, response.status)
    }

    @Test
    fun `detachPolicy throws not found when attachment does not exist`() {
        val policyId = UUID.randomUUID()
        val attachmentId = UUID.randomUUID()

        every { policyAttachmentService.detach(policyId, attachmentId) } returns false

        assertFailsWith<PolicyAttachmentNotFoundException> {
            resource.detachPolicy(policyId.toString(), attachmentId.toString())
        }
    }

    @Test
    fun `listAttachments returns attachments for policy`() {
        val policyId = UUID.randomUUID()
        val attachments = listOf(
            PolicyAttachment(UUID.randomUUID(), policyId, "urn:revet:iam::user/alice"),
            PolicyAttachment(UUID.randomUUID(), policyId, "urn:revet:iam::user/bob")
        )

        every { policyService.findById(policyId) } returns createPolicy("TestPolicy", policyId)
        every { policyAttachmentService.listAttachmentsForPolicy(policyId) } returns attachments

        val result = resource.listAttachments(policyId.toString())

        assertEquals(2, result.items.size)
    }

    private fun createPolicy(name: String, id: UUID = UUID.randomUUID()): Policy = Policy(
        id = id,
        name = name,
        version = "2026-01-15",
        statements = listOf(
            Statement(effect = Effect.ALLOW, actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
        )
    )
}

package com.revethq.iam.serviceaccount.web.api

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.service.AttachedPolicy
import com.revethq.iam.permission.service.PolicyAttachmentService
import com.revethq.iam.serviceaccount.domain.ServiceAccount
import com.revethq.iam.serviceaccount.persistence.service.ServiceAccountService
import com.revethq.iam.serviceaccount.web.exception.ServiceAccountNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServiceAccountPolicyResourceTest {

    private val serviceAccountService = mockk<ServiceAccountService>()
    private val policyAttachmentService = mockk<PolicyAttachmentService>()

    private val resource = ServiceAccountPolicyResource().apply {
        this.serviceAccountService = this@ServiceAccountPolicyResourceTest.serviceAccountService
        this.policyAttachmentService = this@ServiceAccountPolicyResourceTest.policyAttachmentService
    }

    @Test
    fun `listPoliciesForServiceAccount returns attached policies`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot", tenantId = "acme")
        every { serviceAccountService.findById(id) } returns sa

        val attachedPolicies = listOf(
            createAttachedPolicy("AdminPolicy"),
            createAttachedPolicy("ReadOnlyPolicy")
        )
        every { policyAttachmentService.listAttachedPoliciesForPrincipal("urn:revet:iam:acme:service-account/$id") } returns attachedPolicies

        val result = resource.listPoliciesForServiceAccount(id.toString(), 0, 20)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertFalse(result.hasMore)
        assertEquals("AdminPolicy", result.content[0].policy.name)
        assertEquals("ReadOnlyPolicy", result.content[1].policy.name)
        verify { policyAttachmentService.listAttachedPoliciesForPrincipal("urn:revet:iam:acme:service-account/$id") }
    }

    @Test
    fun `listPoliciesForServiceAccount returns empty list when no policies attached`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot")
        every { serviceAccountService.findById(id) } returns sa
        every { policyAttachmentService.listAttachedPoliciesForPrincipal(sa.toUrn()) } returns emptyList()

        val result = resource.listPoliciesForServiceAccount(id.toString(), 0, 20)

        assertEquals(0, result.content.size)
        assertFalse(result.hasMore)
    }

    @Test
    fun `listPoliciesForServiceAccount paginates correctly`() {
        val id = UUID.randomUUID()
        val sa = ServiceAccount(id = id, name = "bot", tenantId = "acme")
        every { serviceAccountService.findById(id) } returns sa

        val attachedPolicies = (1..5).map { createAttachedPolicy("Policy$it") }
        every { policyAttachmentService.listAttachedPoliciesForPrincipal(sa.toUrn()) } returns attachedPolicies

        val page0 = resource.listPoliciesForServiceAccount(id.toString(), 0, 2)
        assertEquals(2, page0.content.size)
        assertEquals("Policy1", page0.content[0].policy.name)
        assertEquals("Policy2", page0.content[1].policy.name)
        assertTrue(page0.hasMore)

        val page1 = resource.listPoliciesForServiceAccount(id.toString(), 1, 2)
        assertEquals(2, page1.content.size)
        assertEquals("Policy3", page1.content[0].policy.name)
        assertEquals("Policy4", page1.content[1].policy.name)
        assertTrue(page1.hasMore)

        val page2 = resource.listPoliciesForServiceAccount(id.toString(), 2, 2)
        assertEquals(1, page2.content.size)
        assertEquals("Policy5", page2.content[0].policy.name)
        assertFalse(page2.hasMore)
    }

    @Test
    fun `listPoliciesForServiceAccount throws not found when service account missing`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.findById(id) } returns null

        assertFailsWith<ServiceAccountNotFoundException> {
            resource.listPoliciesForServiceAccount(id.toString(), 0, 20)
        }
    }

    private fun createAttachedPolicy(name: String): AttachedPolicy = AttachedPolicy(
        attachmentId = UUID.randomUUID(),
        policy = Policy(
            id = UUID.randomUUID(),
            name = name,
            version = "2026-01-15",
            statements = listOf(
                Statement(effect = Effect.ALLOW, actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
            )
        ),
        attachedOn = OffsetDateTime.now(),
        attachedBy = null
    )
}

package com.revethq.iam.permission.web.api

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.persistence.service.PolicyAttachmentService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPolicyResourceTest {

    private val policyAttachmentService = mockk<PolicyAttachmentService>()

    private val resource = UserPolicyResource().apply {
        this.policyAttachmentService = this@UserPolicyResourceTest.policyAttachmentService
    }

    @Test
    fun `listPoliciesForUser returns policies attached to user`() {
        val userId = UUID.randomUUID().toString()
        val policies = listOf(
            createPolicy("AdminPolicy"),
            createPolicy("ReadOnlyPolicy")
        )

        every { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::user/$userId") } returns policies

        val result = resource.listPoliciesForUser(userId, 0, 20)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertFalse(result.hasMore)
        assertEquals("AdminPolicy", result.content[0].name)
        assertEquals("ReadOnlyPolicy", result.content[1].name)
        verify { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::user/$userId") }
    }

    @Test
    fun `listPoliciesForUser returns empty list when no policies attached`() {
        val userId = UUID.randomUUID().toString()

        every { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::user/$userId") } returns emptyList()

        val result = resource.listPoliciesForUser(userId, 0, 20)

        assertEquals(0, result.content.size)
        assertFalse(result.hasMore)
    }

    @Test
    fun `listPoliciesForUser paginates correctly`() {
        val userId = UUID.randomUUID().toString()
        val policies = (1..5).map { createPolicy("Policy$it") }

        every { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::user/$userId") } returns policies

        val page0 = resource.listPoliciesForUser(userId, 0, 2)
        assertEquals(2, page0.content.size)
        assertEquals("Policy1", page0.content[0].name)
        assertEquals("Policy2", page0.content[1].name)
        assertTrue(page0.hasMore)

        val page1 = resource.listPoliciesForUser(userId, 1, 2)
        assertEquals(2, page1.content.size)
        assertEquals("Policy3", page1.content[0].name)
        assertEquals("Policy4", page1.content[1].name)
        assertTrue(page1.hasMore)

        val page2 = resource.listPoliciesForUser(userId, 2, 2)
        assertEquals(1, page2.content.size)
        assertEquals("Policy5", page2.content[0].name)
        assertFalse(page2.hasMore)
    }

    private fun createPolicy(name: String): Policy = Policy(
        id = UUID.randomUUID(),
        name = name,
        version = "2026-01-15",
        statements = listOf(
            Statement(effect = Effect.ALLOW, actions = listOf("iam:*"), resources = listOf("urn:revet:iam:*:*/*"))
        )
    )
}

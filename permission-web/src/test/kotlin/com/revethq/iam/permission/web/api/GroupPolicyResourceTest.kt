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

class GroupPolicyResourceTest {

    private val policyAttachmentService = mockk<PolicyAttachmentService>()

    private val resource = GroupPolicyResource().apply {
        this.policyAttachmentService = this@GroupPolicyResourceTest.policyAttachmentService
    }

    @Test
    fun `listPoliciesForGroup returns policies attached to group`() {
        val groupId = UUID.randomUUID().toString()
        val policies = listOf(
            createPolicy("AdminPolicy"),
            createPolicy("ReadOnlyPolicy")
        )

        every { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::group/$groupId") } returns policies

        val result = resource.listPoliciesForGroup(groupId, 0, 20)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertFalse(result.hasMore)
        assertEquals("AdminPolicy", result.content[0].name)
        assertEquals("ReadOnlyPolicy", result.content[1].name)
        verify { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::group/$groupId") }
    }

    @Test
    fun `listPoliciesForGroup returns empty list when no policies attached`() {
        val groupId = UUID.randomUUID().toString()

        every { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::group/$groupId") } returns emptyList()

        val result = resource.listPoliciesForGroup(groupId, 0, 20)

        assertEquals(0, result.content.size)
        assertFalse(result.hasMore)
    }

    @Test
    fun `listPoliciesForGroup paginates correctly`() {
        val groupId = UUID.randomUUID().toString()
        val policies = (1..5).map { createPolicy("Policy$it") }

        every { policyAttachmentService.listPoliciesForPrincipal("urn:revet:iam::group/$groupId") } returns policies

        val page0 = resource.listPoliciesForGroup(groupId, 0, 2)
        assertEquals(2, page0.content.size)
        assertEquals("Policy1", page0.content[0].name)
        assertEquals("Policy2", page0.content[1].name)
        assertTrue(page0.hasMore)

        val page1 = resource.listPoliciesForGroup(groupId, 1, 2)
        assertEquals(2, page1.content.size)
        assertEquals("Policy3", page1.content[0].name)
        assertEquals("Policy4", page1.content[1].name)
        assertTrue(page1.hasMore)

        val page2 = resource.listPoliciesForGroup(groupId, 2, 2)
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

package com.revethq.iam.permission.evaluation

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.PolicyAttachment
import com.revethq.iam.permission.domain.Statement
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for policy collector implementations.
 * These tests demonstrate the expected behavior of a PolicyCollector
 * and can be used as a reference for implementing the actual collector
 * in the persistence layer.
 */
class PolicyCollectorTest {

    /**
     * Interface for group membership lookup.
     * This would be implemented in the persistence layer.
     */
    interface GroupMembershipService {
        fun getGroupsForUser(userUrn: String): List<String>
    }

    /**
     * Interface for policy attachment lookup.
     * This would be implemented in the persistence layer.
     */
    interface PolicyAttachmentService {
        fun getPoliciesForPrincipal(principalUrn: String): List<Policy>
    }

    /**
     * Example implementation of PolicyCollector for testing.
     */
    class TestPolicyCollector(
        private val groupMembershipService: GroupMembershipService,
        private val policyAttachmentService: PolicyAttachmentService
    ) : PolicyCollector {
        override fun collectPolicies(principalUrn: String): List<Policy> {
            val policies = mutableListOf<Policy>()
            val seenPolicyIds = mutableSetOf<UUID>()

            // Get policies directly attached to the principal
            val userPolicies = policyAttachmentService.getPoliciesForPrincipal(principalUrn)
            for (policy in userPolicies) {
                if (seenPolicyIds.add(policy.id)) {
                    policies.add(policy)
                }
            }

            // Get policies from groups the principal is a member of
            val groups = groupMembershipService.getGroupsForUser(principalUrn)
            for (groupUrn in groups) {
                val groupPolicies = policyAttachmentService.getPoliciesForPrincipal(groupUrn)
                for (policy in groupPolicies) {
                    if (seenPolicyIds.add(policy.id)) {
                        policies.add(policy)
                    }
                }
            }

            return policies
        }
    }

    private val groupMembershipService = mockk<GroupMembershipService>()
    private val policyAttachmentService = mockk<PolicyAttachmentService>()
    private val collector = TestPolicyCollector(groupMembershipService, policyAttachmentService)

    private val userUrn = "urn:revet:iam::user/alice"
    private val groupUrn = "urn:revet:iam:acme-corp:group/admins"

    @Test
    fun `collect policies attached to user`() {
        val userPolicy = createPolicy("UserPolicy")
        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns listOf(userPolicy)
        every { groupMembershipService.getGroupsForUser(userUrn) } returns emptyList()

        val policies = collector.collectPolicies(userUrn)

        assertEquals(1, policies.size)
        assertEquals("UserPolicy", policies[0].name)
    }

    @Test
    fun `collect policies from user's groups`() {
        val groupPolicy = createPolicy("GroupPolicy")
        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns emptyList()
        every { groupMembershipService.getGroupsForUser(userUrn) } returns listOf(groupUrn)
        every { policyAttachmentService.getPoliciesForPrincipal(groupUrn) } returns listOf(groupPolicy)

        val policies = collector.collectPolicies(userUrn)

        assertEquals(1, policies.size)
        assertEquals("GroupPolicy", policies[0].name)
    }

    @Test
    fun `combine user and group policies`() {
        val userPolicy = createPolicy("UserPolicy")
        val groupPolicy = createPolicy("GroupPolicy")
        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns listOf(userPolicy)
        every { groupMembershipService.getGroupsForUser(userUrn) } returns listOf(groupUrn)
        every { policyAttachmentService.getPoliciesForPrincipal(groupUrn) } returns listOf(groupPolicy)

        val policies = collector.collectPolicies(userUrn)

        assertEquals(2, policies.size)
        assertTrue(policies.any { it.name == "UserPolicy" })
        assertTrue(policies.any { it.name == "GroupPolicy" })
    }

    @Test
    fun `handle user with no policies`() {
        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns emptyList()
        every { groupMembershipService.getGroupsForUser(userUrn) } returns emptyList()

        val policies = collector.collectPolicies(userUrn)

        assertTrue(policies.isEmpty())
    }

    @Test
    fun `handle user with no groups`() {
        val userPolicy = createPolicy("UserPolicy")
        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns listOf(userPolicy)
        every { groupMembershipService.getGroupsForUser(userUrn) } returns emptyList()

        val policies = collector.collectPolicies(userUrn)

        assertEquals(1, policies.size)
    }

    @Test
    fun `deduplicate same policy attached to user and group`() {
        val sharedPolicy = createPolicy("SharedPolicy")
        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns listOf(sharedPolicy)
        every { groupMembershipService.getGroupsForUser(userUrn) } returns listOf(groupUrn)
        every { policyAttachmentService.getPoliciesForPrincipal(groupUrn) } returns listOf(sharedPolicy)

        val policies = collector.collectPolicies(userUrn)

        assertEquals(1, policies.size)  // Should only appear once
        assertEquals("SharedPolicy", policies[0].name)
    }

    @Test
    fun `collect policies from multiple groups`() {
        val group1Urn = "urn:revet:iam:acme-corp:group/developers"
        val group2Urn = "urn:revet:iam:acme-corp:group/testers"
        val policy1 = createPolicy("DeveloperPolicy")
        val policy2 = createPolicy("TesterPolicy")

        every { policyAttachmentService.getPoliciesForPrincipal(userUrn) } returns emptyList()
        every { groupMembershipService.getGroupsForUser(userUrn) } returns listOf(group1Urn, group2Urn)
        every { policyAttachmentService.getPoliciesForPrincipal(group1Urn) } returns listOf(policy1)
        every { policyAttachmentService.getPoliciesForPrincipal(group2Urn) } returns listOf(policy2)

        val policies = collector.collectPolicies(userUrn)

        assertEquals(2, policies.size)
        assertTrue(policies.any { it.name == "DeveloperPolicy" })
        assertTrue(policies.any { it.name == "TesterPolicy" })
    }

    private fun createPolicy(name: String): Policy {
        return Policy(
            id = UUID.randomUUID(),
            name = name,
            version = "2026-01-15",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("*"),
                    resources = listOf("urn:revet:*:*:*/*")
                )
            )
        )
    }
}

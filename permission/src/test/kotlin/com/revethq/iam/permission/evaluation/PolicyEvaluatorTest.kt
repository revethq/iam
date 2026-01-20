package com.revethq.iam.permission.evaluation

import com.revethq.iam.permission.condition.ConditionContext
import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyEvaluatorTest {

    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val principalUrn = "urn:revet:iam::user/alice"
    private val resourceUrn = "urn:revet:iam:acme-corp:user/bob"
    private val action = "iam:GetUser"

    @Test
    fun `implicit deny when no policies attached`() {
        every { policyCollector.collectPolicies(principalUrn) } returns emptyList()

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
        assertTrue(result.matchingStatements.isEmpty())
    }

    @Test
    fun `single Allow statement grants access`() {
        val policy = createPolicy(
            name = "AllowGetUser",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isAllowed())
        assertEquals(1, result.matchingStatements.size)
        assertEquals("AllowGetUser", result.matchingStatements[0].policyName)
    }

    @Test
    fun `single Deny statement denies access`() {
        val policy = createPolicy(
            name = "DenyGetUser",
            statements = listOf(
                Statement(
                    effect = Effect.DENY,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isDenied())
        assertTrue(result.isExplicitDeny)
        assertEquals(1, result.matchingStatements.size)
    }

    @Test
    fun `explicit deny overrides allow - two policies`() {
        val allowPolicy = createPolicy(
            name = "AllowAll",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:*"),
                    resources = listOf("urn:revet:iam:*:user/*")
                )
            )
        )
        val denyPolicy = createPolicy(
            name = "DenySpecific",
            statements = listOf(
                Statement(
                    effect = Effect.DENY,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/bob")
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(allowPolicy, denyPolicy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isDenied())
        assertTrue(result.isExplicitDeny)
    }

    @Test
    fun `condition failure prevents statement match`() {
        val policy = createPolicy(
            name = "AllowWithCondition",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*"),
                    conditions = mapOf(
                        "IpAddress" to mapOf(
                            "revet:SourceIp" to listOf("10.0.0.0/8")
                        )
                    )
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn,
            context = ConditionContext(sourceIp = "192.168.1.1")  // Not in 10.0.0.0/8
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)  // Implicit deny because condition didn't match
    }

    @Test
    fun `condition success allows statement match`() {
        val policy = createPolicy(
            name = "AllowWithCondition",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*"),
                    conditions = mapOf(
                        "IpAddress" to mapOf(
                            "revet:SourceIp" to listOf("10.0.0.0/8")
                        )
                    )
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn,
            context = ConditionContext(sourceIp = "10.0.0.1")  // In 10.0.0.0/8
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isAllowed())
    }

    @Test
    fun `action mismatch prevents statement match`() {
        val policy = createPolicy(
            name = "AllowCreateUser",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:CreateUser"),  // Different action
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = "iam:GetUser",
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `resource mismatch prevents statement match`() {
        val policy = createPolicy(
            name = "AllowOtherTenant",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:other-corp:user/*")  // Different tenant
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn  // acme-corp, not other-corp
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `wildcard action matches`() {
        val policy = createPolicy(
            name = "AllowAllIam",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:*"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = "iam:GetUser",
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluate(request)

        assertTrue(result.isAllowed())
    }

    @Test
    fun `multiple policies combined correctly`() {
        val policy1 = createPolicy(
            name = "AllowRead",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser", "iam:ListUsers"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )
        val policy2 = createPolicy(
            name = "AllowWrite",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:CreateUser", "iam:UpdateUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy1, policy2)

        // Test read action
        val readResult = evaluator.evaluate(
            AuthorizationRequest(principalUrn, "iam:GetUser", resourceUrn)
        )
        assertTrue(readResult.isAllowed())
        assertEquals("AllowRead", readResult.matchingStatements[0].policyName)

        // Test write action
        val writeResult = evaluator.evaluate(
            AuthorizationRequest(principalUrn, "iam:CreateUser", resourceUrn)
        )
        assertTrue(writeResult.isAllowed())
        assertEquals("AllowWrite", writeResult.matchingStatements[0].policyName)

        // Test denied action
        val deleteResult = evaluator.evaluate(
            AuthorizationRequest(principalUrn, "iam:DeleteUser", resourceUrn)
        )
        assertTrue(deleteResult.isDenied())
    }

    @Test
    fun `evaluateWithPolicies works without collector`() {
        val policy = createPolicy(
            name = "DirectPolicy",
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser"),
                    resources = listOf("urn:revet:iam:acme-corp:user/*")
                )
            )
        )

        val request = AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = resourceUrn
        )

        val result = evaluator.evaluateWithPolicies(request, listOf(policy))

        assertTrue(result.isAllowed())
    }

    private fun createPolicy(name: String, statements: List<Statement>): Policy {
        return Policy(
            id = UUID.randomUUID(),
            name = name,
            version = "2026-01-15",
            statements = statements
        )
    }
}

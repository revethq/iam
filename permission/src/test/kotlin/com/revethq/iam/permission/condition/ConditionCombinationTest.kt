package com.revethq.iam.permission.condition

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionCombinationTest {

    @Test
    fun `multiple conditions AND together - all pass`() {
        val conditions = mapOf(
            "StringEquals" to mapOf(
                "revet:PrincipalId" to listOf("urn:revet:iam::user/alice")
            ),
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("10.0.0.0/8")
            )
        )

        val context = ConditionContext(
            principalId = "urn:revet:iam::user/alice",
            sourceIp = "10.0.0.1"
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `multiple conditions AND together - one fails`() {
        val conditions = mapOf(
            "StringEquals" to mapOf(
                "revet:PrincipalId" to listOf("urn:revet:iam::user/alice")
            ),
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("10.0.0.0/8")
            )
        )

        val context = ConditionContext(
            principalId = "urn:revet:iam::user/alice",
            sourceIp = "192.168.1.1"  // Not in 10.0.0.0/8
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `multiple keys within same operator AND together`() {
        val conditions = mapOf(
            "StringEquals" to mapOf(
                "revet:PrincipalId" to listOf("urn:revet:iam::user/alice"),
                "revet:RequestedAction" to listOf("iam:GetUser")
            )
        )

        // Both keys match
        assertTrue(
            ConditionEvaluator.evaluate(
                conditions,
                ConditionContext(
                    principalId = "urn:revet:iam::user/alice",
                    requestedAction = "iam:GetUser"
                )
            )
        )

        // First key matches, second doesn't
        assertFalse(
            ConditionEvaluator.evaluate(
                conditions,
                ConditionContext(
                    principalId = "urn:revet:iam::user/alice",
                    requestedAction = "iam:DeleteUser"
                )
            )
        )

        // First key doesn't match, second does
        assertFalse(
            ConditionEvaluator.evaluate(
                conditions,
                ConditionContext(
                    principalId = "urn:revet:iam::user/bob",
                    requestedAction = "iam:GetUser"
                )
            )
        )
    }

    @Test
    fun `multiple values for same key OR together - one matches`() {
        val conditions = mapOf(
            "StringEquals" to mapOf(
                "revet:PrincipalId" to listOf(
                    "urn:revet:iam::user/alice",
                    "urn:revet:iam::user/bob",
                    "urn:revet:iam::user/charlie"
                )
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/alice")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/bob")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/charlie")))
    }

    @Test
    fun `multiple values for same key OR together - none match`() {
        val conditions = mapOf(
            "StringEquals" to mapOf(
                "revet:PrincipalId" to listOf(
                    "urn:revet:iam::user/alice",
                    "urn:revet:iam::user/bob"
                )
            )
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/david")))
    }

    @Test
    fun `empty conditions always pass`() {
        val conditions = emptyMap<String, Map<String, List<String>>>()

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext()))
    }

    @Test
    fun `complex condition with variable resolution`() {
        val conditions = mapOf(
            "StringEquals" to mapOf(
                "revet:PrincipalId" to listOf("\${revet:PrincipalId}")  // Self-referential for demo
            )
        )

        val context = ConditionContext(principalId = "urn:revet:iam::user/alice")

        // The condition value resolves to the same as context value
        assertTrue(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `multiple IP address ranges OR together`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf(
                    "10.0.0.0/8",
                    "172.16.0.0/12",
                    "192.168.0.0/16"
                )
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "10.0.0.1")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "172.16.0.1")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.1")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "8.8.8.8")))
    }
}

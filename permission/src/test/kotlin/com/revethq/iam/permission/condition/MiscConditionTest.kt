package com.revethq.iam.permission.condition

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiscConditionTest {

    // ==================== Bool Operator Tests ====================

    @Test
    fun `Bool operator true`() {
        val conditions = mapOf(
            "Bool" to mapOf(
                "customKey" to listOf("true")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "true"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "false"))))
    }

    @Test
    fun `Bool operator false`() {
        val conditions = mapOf(
            "Bool" to mapOf(
                "customKey" to listOf("false")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "false"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "true"))))
    }

    @Test
    fun `Bool operator fails for non-boolean values`() {
        val conditions = mapOf(
            "Bool" to mapOf(
                "customKey" to listOf("true")
            )
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "yes"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "1"))))
    }

    // ==================== IpAddress Operator Tests ====================

    @Test
    fun `IpAddress CIDR matching - class A network`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("10.0.0.0/8")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "10.0.0.1")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "10.255.255.255")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "10.100.50.25")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "11.0.0.1")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.1")))
    }

    @Test
    fun `IpAddress CIDR matching - class B network`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("172.16.0.0/12")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "172.16.0.1")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "172.31.255.255")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "172.32.0.1")))
    }

    @Test
    fun `IpAddress CIDR matching - class C network`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("192.168.1.0/24")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.1")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.255")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.2.1")))
    }

    @Test
    fun `IpAddress exact match without CIDR`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("192.168.1.100")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.100")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.101")))
    }

    @Test
    fun `IpAddress with multiple CIDR ranges`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("10.0.0.0/8", "192.168.0.0/16")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "10.0.0.1")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.1")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "172.16.0.1")))
    }

    @Test
    fun `NotIpAddress`() {
        val conditions = mapOf(
            "NotIpAddress" to mapOf(
                "revet:SourceIp" to listOf("10.0.0.0/8")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "192.168.1.1")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "10.0.0.1")))
    }

    @Test
    fun `IpAddress with invalid IP returns false`() {
        val conditions = mapOf(
            "IpAddress" to mapOf(
                "revet:SourceIp" to listOf("10.0.0.0/8")
            )
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(sourceIp = "invalid-ip")))
    }

    // ==================== Null Operator Tests ====================

    @Test
    fun `Null existence check - key exists when expecting null`() {
        val conditions = mapOf(
            "Null" to mapOf(
                "customKey" to listOf("true")
            )
        )

        // Expecting null (true) but key exists -> false
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "value"))))
    }

    @Test
    fun `Null existence check - key missing when expecting null`() {
        val conditions = mapOf(
            "Null" to mapOf(
                "customKey" to listOf("true")
            )
        )

        // Expecting null (true) and key is missing -> true
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext()))
    }

    @Test
    fun `Null existence check - key exists when expecting not null`() {
        val conditions = mapOf(
            "Null" to mapOf(
                "customKey" to listOf("false")
            )
        )

        // Expecting not null (false) and key exists -> true
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "value"))))
    }

    @Test
    fun `Null existence check - key missing when expecting not null`() {
        val conditions = mapOf(
            "Null" to mapOf(
                "customKey" to listOf("false")
            )
        )

        // Expecting not null (false) but key is missing -> false
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext()))
    }

    // ==================== Unknown Operator Test ====================

    @Test
    fun `Unknown operator fails evaluation`() {
        val conditions = mapOf(
            "UnknownOperator" to mapOf(
                "customKey" to listOf("value")
            )
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "value"))))
    }
}

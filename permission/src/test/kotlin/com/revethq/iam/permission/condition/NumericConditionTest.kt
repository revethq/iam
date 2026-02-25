package com.revethq.iam.permission.condition

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericConditionTest {
    @Test
    fun `NumericEquals`() {
        val conditions =
            mapOf(
                "NumericEquals" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100.0"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "99"))))
    }

    @Test
    fun `NumericNotEquals`() {
        val conditions =
            mapOf(
                "NumericNotEquals" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "99"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "101"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
    }

    @Test
    fun `NumericLessThan`() {
        val conditions =
            mapOf(
                "NumericLessThan" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "99"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "50"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "101"))))
    }

    @Test
    fun `NumericLessThanEquals`() {
        val conditions =
            mapOf(
                "NumericLessThanEquals" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "99"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "101"))))
    }

    @Test
    fun `NumericGreaterThan`() {
        val conditions =
            mapOf(
                "NumericGreaterThan" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "101"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "200"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "99"))))
    }

    @Test
    fun `NumericGreaterThanEquals`() {
        val conditions =
            mapOf(
                "NumericGreaterThanEquals" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "101"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "99"))))
    }

    @Test
    fun `NumericEquals with decimal values`() {
        val conditions =
            mapOf(
                "NumericEquals" to
                    mapOf(
                        "customKey" to listOf("100.50"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100.50"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100.5"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100.51"))))
    }

    @Test
    fun `NumericEquals with negative values`() {
        val conditions =
            mapOf(
                "NumericGreaterThan" to
                    mapOf(
                        "customKey" to listOf("-10"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "0"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "-5"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "-10"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "-20"))))
    }

    @Test
    fun `Invalid numeric value handling - context value not numeric`() {
        val conditions =
            mapOf(
                "NumericEquals" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "not-a-number"))))
    }

    @Test
    fun `Invalid numeric value handling - condition value not numeric`() {
        val conditions =
            mapOf(
                "NumericEquals" to
                    mapOf(
                        "customKey" to listOf("not-a-number"),
                    ),
            )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "100"))))
    }

    @Test
    fun `Numeric condition fails when context value is null`() {
        val conditions =
            mapOf(
                "NumericEquals" to
                    mapOf(
                        "customKey" to listOf("100"),
                    ),
            )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext()))
    }
}

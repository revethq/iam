package com.revethq.iam.permission.condition

import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateConditionTest {

    private val baseTime = OffsetDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC)

    @Test
    fun `DateEquals`() {
        val conditions = mapOf(
            "DateEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:01Z"))))
    }

    @Test
    fun `DateNotEquals`() {
        val conditions = mapOf(
            "DateNotEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:01Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
    }

    @Test
    fun `DateLessThan`() {
        val conditions = mapOf(
            "DateLessThan" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T11:59:59Z"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-14T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:01Z"))))
    }

    @Test
    fun `DateLessThanEquals`() {
        val conditions = mapOf(
            "DateLessThanEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T11:59:59Z"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:01Z"))))
    }

    @Test
    fun `DateGreaterThan`() {
        val conditions = mapOf(
            "DateGreaterThan" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:01Z"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-16T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T11:59:59Z"))))
    }

    @Test
    fun `DateGreaterThanEquals`() {
        val conditions = mapOf(
            "DateGreaterThanEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:01Z"))))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T11:59:59Z"))))
    }

    @Test
    fun `ISO 8601 format parsing with timezone offset`() {
        val conditions = mapOf(
            "DateEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00+00:00")
            )
        )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15T12:00:00Z"))))
    }

    @Test
    fun `DateLessThan with CurrentTime context variable`() {
        // Test that current time can be used in date comparisons
        val conditions = mapOf(
            "DateLessThan" to mapOf(
                "revet:CurrentTime" to listOf("2030-01-01T00:00:00Z")
            )
        )

        // CurrentTime should be before 2030
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(currentTime = baseTime)))
    }

    @Test
    fun `Invalid date format returns false`() {
        val conditions = mapOf(
            "DateEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "invalid-date"))))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(customVariables = mapOf("customKey" to "2026-01-15"))))
    }

    @Test
    fun `Date condition fails when context value is null`() {
        val conditions = mapOf(
            "DateEquals" to mapOf(
                "customKey" to listOf("2026-01-15T12:00:00Z")
            )
        )

        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext()))
    }
}

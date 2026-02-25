package com.revethq.iam.permission.condition

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringConditionTest {
    @Test
    fun `StringEquals exact match`() {
        val conditions =
            mapOf(
                "StringEquals" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/alice"),
                    ),
            )
        val context = ConditionContext(principalId = "urn:revet:iam::user/alice")

        assertTrue(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `StringEquals case sensitivity`() {
        val conditions =
            mapOf(
                "StringEquals" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/Alice"),
                    ),
            )
        val context = ConditionContext(principalId = "urn:revet:iam::user/alice")

        assertFalse(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `StringEquals fails when no match`() {
        val conditions =
            mapOf(
                "StringEquals" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/bob"),
                    ),
            )
        val context = ConditionContext(principalId = "urn:revet:iam::user/alice")

        assertFalse(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `StringEquals with multiple values uses OR`() {
        val conditions =
            mapOf(
                "StringEquals" to
                    mapOf(
                        "revet:PrincipalId" to
                            listOf(
                                "urn:revet:iam::user/alice",
                                "urn:revet:iam::user/bob",
                            ),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/alice")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/bob")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/charlie")))
    }

    @Test
    fun `StringNotEquals`() {
        val conditions =
            mapOf(
                "StringNotEquals" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/admin"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/alice")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/admin")))
    }

    @Test
    fun `StringEqualsIgnoreCase`() {
        val conditions =
            mapOf(
                "StringEqualsIgnoreCase" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/ALICE"),
                    ),
            )
        val context = ConditionContext(principalId = "urn:revet:iam::user/alice")

        assertTrue(ConditionEvaluator.evaluate(conditions, context))
    }

    @Test
    fun `StringNotEqualsIgnoreCase`() {
        val conditions =
            mapOf(
                "StringNotEqualsIgnoreCase" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/ADMIN"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/alice")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/admin")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/ADMIN")))
    }

    @Test
    fun `StringLike with asterisk wildcard`() {
        val conditions =
            mapOf(
                "StringLike" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/*"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/alice")))
        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::user/bob")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(principalId = "urn:revet:iam::group/admins")))
    }

    @Test
    fun `StringLike with question mark wildcard`() {
        val conditions =
            mapOf(
                "StringLike" to
                    mapOf(
                        "revet:RequestedAction" to listOf("iam:Get????"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(requestedAction = "iam:GetUser")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(requestedAction = "iam:GetUsers")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(requestedAction = "iam:Get")))
    }

    @Test
    fun `StringLike with combined wildcards`() {
        val conditions =
            mapOf(
                "StringLike" to
                    mapOf(
                        "revet:RequestedResource" to listOf("urn:revet:storage:*:bucket/reports-*"),
                    ),
            )

        assertTrue(
            ConditionEvaluator.evaluate(conditions, ConditionContext(requestedResource = "urn:revet:storage:acme:bucket/reports-2024")),
        )
        assertTrue(
            ConditionEvaluator.evaluate(conditions, ConditionContext(requestedResource = "urn:revet:storage:other:bucket/reports-q1")),
        )
        assertFalse(
            ConditionEvaluator.evaluate(conditions, ConditionContext(requestedResource = "urn:revet:storage:acme:bucket/logs-2024")),
        )
    }

    @Test
    fun `StringNotLike`() {
        val conditions =
            mapOf(
                "StringNotLike" to
                    mapOf(
                        "revet:RequestedResource" to listOf("urn:revet:iam::user/admin*"),
                    ),
            )

        assertTrue(ConditionEvaluator.evaluate(conditions, ConditionContext(requestedResource = "urn:revet:iam::user/alice")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(requestedResource = "urn:revet:iam::user/admin")))
        assertFalse(ConditionEvaluator.evaluate(conditions, ConditionContext(requestedResource = "urn:revet:iam::user/admin-backup")))
    }

    @Test
    fun `String condition fails when context value is null`() {
        val conditions =
            mapOf(
                "StringEquals" to
                    mapOf(
                        "revet:PrincipalId" to listOf("urn:revet:iam::user/alice"),
                    ),
            )
        val context = ConditionContext(principalId = null)

        assertFalse(ConditionEvaluator.evaluate(conditions, context))
    }
}

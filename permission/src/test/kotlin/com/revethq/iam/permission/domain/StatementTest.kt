package com.revethq.iam.permission.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatementTest {
    @Test
    fun `valid statement with all required fields`() {
        val statement =
            Statement(
                sid = "AllowReadUsers",
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser", "iam:ListUsers"),
                resources = listOf("urn:revet:iam:acme-corp:user/*"),
            )

        assertEquals("AllowReadUsers", statement.sid)
        assertEquals(Effect.ALLOW, statement.effect)
        assertEquals(listOf("iam:GetUser", "iam:ListUsers"), statement.actions)
        assertEquals(listOf("urn:revet:iam:acme-corp:user/*"), statement.resources)
        assertTrue(statement.conditions.isEmpty())
    }

    @Test
    fun `valid statement without optional sid`() {
        val statement =
            Statement(
                effect = Effect.DENY,
                actions = listOf("iam:DeleteUser"),
                resources = listOf("urn:revet:iam::user/admin"),
            )

        assertEquals(null, statement.sid)
        assertEquals(Effect.DENY, statement.effect)
    }

    @Test
    fun `valid statement with conditions`() {
        val conditions =
            mapOf(
                "IpAddress" to
                    mapOf(
                        "revet:SourceIp" to listOf("10.0.0.0/8", "192.168.0.0/16"),
                    ),
            )
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:*"),
                resources = listOf("urn:revet:iam:acme-corp:user/*"),
                conditions = conditions,
            )

        assertEquals(conditions, statement.conditions)
    }

    @Test
    fun `reject statement without actions`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                Statement(
                    effect = Effect.ALLOW,
                    actions = emptyList(),
                    resources = listOf("urn:revet:iam::user/*"),
                )
            }
        assertEquals("Statement must have at least one action", exception.message)
    }

    @Test
    fun `reject statement without resources`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf("iam:GetUser"),
                    resources = emptyList(),
                )
            }
        assertEquals("Statement must have at least one resource", exception.message)
    }

    @Test
    fun `matchesAction returns true for exact match`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:CreateUser"),
                resources = listOf("urn:revet:iam::user/*"),
            )

        assertTrue(statement.matchesAction("iam:CreateUser"))
    }

    @Test
    fun `matchesAction returns true for wildcard match`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:*"),
                resources = listOf("urn:revet:iam::user/*"),
            )

        assertTrue(statement.matchesAction("iam:CreateUser"))
        assertTrue(statement.matchesAction("iam:DeleteUser"))
    }

    @Test
    fun `matchesAction returns false when no match`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:CreateUser"),
                resources = listOf("urn:revet:iam::user/*"),
            )

        assertFalse(statement.matchesAction("iam:DeleteUser"))
    }

    @Test
    fun `matchesResource returns true for exact match`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser"),
                resources = listOf("urn:revet:iam:acme-corp:user/alice"),
            )

        assertTrue(statement.matchesResource("urn:revet:iam:acme-corp:user/alice"))
    }

    @Test
    fun `matchesResource returns true for wildcard match`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser"),
                resources = listOf("urn:revet:iam:acme-corp:user/*"),
            )

        assertTrue(statement.matchesResource("urn:revet:iam:acme-corp:user/alice"))
        assertTrue(statement.matchesResource("urn:revet:iam:acme-corp:user/bob"))
    }

    @Test
    fun `matchesResource returns false when no match`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser"),
                resources = listOf("urn:revet:iam:acme-corp:user/*"),
            )

        assertFalse(statement.matchesResource("urn:revet:iam:other-corp:user/alice"))
    }

    @Test
    fun `matchesResource returns false for invalid URN`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser"),
                resources = listOf("urn:revet:iam:acme-corp:user/*"),
            )

        assertFalse(statement.matchesResource("invalid-urn"))
    }

    @Test
    fun `matchesResource with Urn object`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser"),
                resources = listOf("urn:revet:iam:acme-corp:user/*"),
            )
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")

        assertTrue(statement.matchesResource(urn))
    }

    @Test
    fun `matchesResource with multiple resource patterns`() {
        val statement =
            Statement(
                effect = Effect.ALLOW,
                actions = listOf("iam:GetUser"),
                resources =
                    listOf(
                        "urn:revet:iam:acme-corp:user/*",
                        "urn:revet:iam:other-corp:user/*",
                    ),
            )

        assertTrue(statement.matchesResource("urn:revet:iam:acme-corp:user/alice"))
        assertTrue(statement.matchesResource("urn:revet:iam:other-corp:user/bob"))
        assertFalse(statement.matchesResource("urn:revet:iam:third-corp:user/charlie"))
    }
}

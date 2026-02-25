package com.revethq.iam.permission.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionMatcherTest {
    @Test
    fun `exact action match`() {
        assertTrue(ActionMatcher.matches("iam:CreateUser", "iam:CreateUser"))
    }

    @Test
    fun `service wildcard match`() {
        assertTrue(ActionMatcher.matches("iam:CreateUser", "iam:*"))
        assertTrue(ActionMatcher.matches("iam:DeleteUser", "iam:*"))
        assertTrue(ActionMatcher.matches("iam:GetUser", "iam:*"))
    }

    @Test
    fun `full wildcard match`() {
        assertTrue(ActionMatcher.matches("iam:CreateUser", "*"))
        assertTrue(ActionMatcher.matches("storage:GetObject", "*"))
        assertTrue(ActionMatcher.matches("anything:here", "*"))
    }

    @Test
    fun `no match when action differs`() {
        assertFalse(ActionMatcher.matches("iam:CreateUser", "iam:DeleteUser"))
    }

    @Test
    fun `no match when service differs`() {
        assertFalse(ActionMatcher.matches("iam:CreateUser", "storage:CreateUser"))
    }

    @Test
    fun `service wildcard does not match different service`() {
        assertFalse(ActionMatcher.matches("storage:CreateBucket", "iam:*"))
    }

    @Test
    fun `matchesAny returns true if any pattern matches`() {
        val patterns = listOf("iam:CreateUser", "iam:DeleteUser", "storage:*")

        assertTrue(ActionMatcher.matchesAny("iam:CreateUser", patterns))
        assertTrue(ActionMatcher.matchesAny("iam:DeleteUser", patterns))
        assertTrue(ActionMatcher.matchesAny("storage:GetObject", patterns))
    }

    @Test
    fun `matchesAny returns false if no pattern matches`() {
        val patterns = listOf("iam:CreateUser", "iam:DeleteUser")

        assertFalse(ActionMatcher.matchesAny("iam:GetUser", patterns))
        assertFalse(ActionMatcher.matchesAny("storage:GetObject", patterns))
    }

    @Test
    fun `matchesAny with empty list returns false`() {
        assertFalse(ActionMatcher.matchesAny("iam:CreateUser", emptyList()))
    }

    @Test
    fun `invalid action format falls back to exact match`() {
        assertTrue(ActionMatcher.matches("invalidAction", "invalidAction"))
        assertFalse(ActionMatcher.matches("invalidAction", "otherAction"))
    }

    @Test
    fun `invalid pattern format falls back to exact match`() {
        assertFalse(ActionMatcher.matches("iam:CreateUser", "invalidPattern"))
    }
}

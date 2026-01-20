package com.revethq.iam.permission.condition

import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionContextTest {

    private val testTime = OffsetDateTime.of(2026, 1, 15, 12, 30, 45, 0, ZoneOffset.UTC)

    @Test
    fun `resolve PrincipalId variable`() {
        val context = ConditionContext(principalId = "urn:revet:iam::user/alice")

        assertEquals("urn:revet:iam::user/alice", context.resolveVariable("\${revet:PrincipalId}"))
    }

    @Test
    fun `resolve CurrentTime variable`() {
        val context = ConditionContext(currentTime = testTime)

        assertEquals("2026-01-15T12:30:45Z", context.resolveVariable("\${revet:CurrentTime}"))
    }

    @Test
    fun `resolve SourceIp variable`() {
        val context = ConditionContext(sourceIp = "192.168.1.100")

        assertEquals("192.168.1.100", context.resolveVariable("\${revet:SourceIp}"))
    }

    @Test
    fun `resolve RequestedAction variable`() {
        val context = ConditionContext(requestedAction = "iam:CreateUser")

        assertEquals("iam:CreateUser", context.resolveVariable("\${revet:RequestedAction}"))
    }

    @Test
    fun `resolve RequestedResource variable`() {
        val context = ConditionContext(requestedResource = "urn:revet:iam:acme-corp:user/alice")

        assertEquals("urn:revet:iam:acme-corp:user/alice", context.resolveVariable("\${revet:RequestedResource}"))
    }

    @Test
    fun `unknown variable resolves to empty string`() {
        val context = ConditionContext()

        assertEquals("", context.resolveVariable("\${revet:UnknownVariable}"))
    }

    @Test
    fun `null variable resolves to empty string`() {
        val context = ConditionContext(principalId = null)

        assertEquals("", context.resolveVariable("\${revet:PrincipalId}"))
    }

    @Test
    fun `non-variable string returned unchanged`() {
        val context = ConditionContext()

        assertEquals("literal-value", context.resolveVariable("literal-value"))
    }

    @Test
    fun `resolve custom variable`() {
        val context = ConditionContext(
            customVariables = mapOf("custom:MyKey" to "my-value")
        )

        assertEquals("my-value", context.resolveVariable("\${custom:MyKey}"))
    }

    @Test
    fun `resolveVariables replaces all variables in string`() {
        val context = ConditionContext(
            principalId = "alice",
            requestedAction = "iam:CreateUser"
        )

        val result = context.resolveVariables("User \${revet:PrincipalId} requested \${revet:RequestedAction}")

        assertEquals("User alice requested iam:CreateUser", result)
    }

    @Test
    fun `resolveVariables handles string with no variables`() {
        val context = ConditionContext()

        val result = context.resolveVariables("no variables here")

        assertEquals("no variables here", result)
    }

    @Test
    fun `getValue returns correct values`() {
        val context = ConditionContext(
            principalId = "alice",
            sourceIp = "10.0.0.1",
            requestedAction = "iam:GetUser",
            requestedResource = "urn:revet:iam::user/bob",
            currentTime = testTime
        )

        assertEquals("alice", context.getValue("revet:PrincipalId"))
        assertEquals("10.0.0.1", context.getValue("revet:SourceIp"))
        assertEquals("iam:GetUser", context.getValue("revet:RequestedAction"))
        assertEquals("urn:revet:iam::user/bob", context.getValue("revet:RequestedResource"))
        assertEquals("2026-01-15T12:30:45Z", context.getValue("revet:CurrentTime"))
    }

    @Test
    fun `getValue returns null for missing keys`() {
        val context = ConditionContext()

        assertEquals(null, context.getValue("revet:PrincipalId"))
        assertEquals(null, context.getValue("revet:SourceIp"))
        assertEquals(null, context.getValue("unknown:Key"))
    }

    @Test
    fun `hasKey returns true when key exists`() {
        val context = ConditionContext(principalId = "alice")

        assertEquals(true, context.hasKey("revet:PrincipalId"))
    }

    @Test
    fun `hasKey returns false when key is missing`() {
        val context = ConditionContext()

        assertEquals(false, context.hasKey("revet:PrincipalId"))
        assertEquals(false, context.hasKey("unknown:Key"))
    }
}

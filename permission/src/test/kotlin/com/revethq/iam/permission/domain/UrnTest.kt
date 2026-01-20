package com.revethq.iam.permission.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UrnTest {

    // ==================== Parsing Tests ====================

    @Test
    fun `parse valid URN with tenant extracts all components`() {
        val urn = Urn.parse("urn:revet:iam:acme-corp:group/admins")

        assertNotNull(urn)
        assertEquals("revet", urn.namespace)
        assertEquals("iam", urn.service)
        assertEquals("acme-corp", urn.tenant)
        assertEquals("group", urn.resourceType)
        assertEquals("admins", urn.resourceId)
    }

    @Test
    fun `parse valid URN without tenant has empty tenant`() {
        val urn = Urn.parse("urn:revet:iam::user/alice")

        assertNotNull(urn)
        assertEquals("revet", urn.namespace)
        assertEquals("iam", urn.service)
        assertEquals("", urn.tenant)
        assertEquals("user", urn.resourceType)
        assertEquals("alice", urn.resourceId)
    }

    @Test
    fun `parse URN with custom namespace`() {
        val urn = Urn.parse("urn:acme:compute:prod:instance/i-12345")

        assertNotNull(urn)
        assertEquals("acme", urn.namespace)
        assertEquals("compute", urn.service)
        assertEquals("prod", urn.tenant)
        assertEquals("instance", urn.resourceType)
        assertEquals("i-12345", urn.resourceId)
    }

    @Test
    fun `parse URN with path segments in resourceId`() {
        val urn = Urn.parse("urn:revet:storage:acme-corp:bucket/reports/2024/q1")

        assertNotNull(urn)
        assertEquals("revet", urn.namespace)
        assertEquals("storage", urn.service)
        assertEquals("acme-corp", urn.tenant)
        assertEquals("bucket", urn.resourceType)
        assertEquals("reports/2024/q1", urn.resourceId)
    }

    @Test
    fun `reject invalid URN format - missing prefix`() {
        assertNull(Urn.parse("revet:iam::user/alice"))
    }

    @Test
    fun `reject invalid URN format - wrong prefix`() {
        assertNull(Urn.parse("arn:revet:iam::user/alice"))
    }

    @Test
    fun `reject URN with missing components`() {
        assertNull(Urn.parse("urn:revet:iam::user"))
        assertNull(Urn.parse("urn:revet:iam:user/alice"))
        assertNull(Urn.parse("urn:revet::user/alice"))
        assertNull(Urn.parse("urn::iam::user/alice"))
    }

    @Test
    fun `toString produces valid URN string`() {
        val urn = Urn(
            namespace = "revet",
            service = "iam",
            tenant = "acme-corp",
            resourceType = "user",
            resourceId = "alice"
        )

        assertEquals("urn:revet:iam:acme-corp:user/alice", urn.toString())
    }

    @Test
    fun `toString with empty tenant`() {
        val urn = Urn(
            namespace = "revet",
            service = "iam",
            tenant = "",
            resourceType = "user",
            resourceId = "alice"
        )

        assertEquals("urn:revet:iam::user/alice", urn.toString())
    }

    // ==================== Wildcard Matching Tests ====================

    @Test
    fun `exact match succeeds`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `exact match fails when different`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/bob")

        assertFalse(urn.matches(pattern))
    }

    @Test
    fun `single segment wildcard matches one segment`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/*")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `single segment wildcard does not match nested paths`() {
        val urn = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/2024")
        val pattern = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/*")

        assertFalse(urn.matches(pattern))
    }

    @Test
    fun `multi-segment wildcard matches nested paths`() {
        val urn = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/2024/q1")
        val pattern = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/**")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `multi-segment wildcard matches single segment`() {
        val urn = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports")
        val pattern = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/**")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `wildcard in middle of path`() {
        val urn = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/2024/data.csv")
        val pattern = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/*/data.csv")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `wildcard in middle of path does not match multiple segments`() {
        val urn = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/2024/q1/data.csv")
        val pattern = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/*/data.csv")

        assertFalse(urn.matches(pattern))
    }

    @Test
    fun `multi-segment wildcard in middle of path matches multiple segments`() {
        val urn = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/2024/q1/data.csv")
        val pattern = Urn.parseOrThrow("urn:revet:storage:acme-corp:bucket/reports/**/data.csv")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `wildcard in namespace component`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:*:iam:acme-corp:user/alice")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `wildcard in service component`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:revet:*:acme-corp:user/alice")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `wildcard in tenant component`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:revet:iam:*:user/alice")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `wildcard in resource type component`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")
        val pattern = Urn.parseOrThrow("urn:revet:iam:acme-corp:*/alice")

        assertTrue(urn.matches(pattern))
    }

    @Test
    fun `matches string pattern`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")

        assertTrue(urn.matches("urn:revet:iam:acme-corp:user/*"))
        assertFalse(urn.matches("urn:revet:iam:other-corp:user/*"))
    }

    @Test
    fun `matches returns false for invalid pattern string`() {
        val urn = Urn.parseOrThrow("urn:revet:iam:acme-corp:user/alice")

        assertFalse(urn.matches("invalid-pattern"))
    }
}

package com.revethq.iam.scim.filter

import com.revethq.iam.scim.exception.ScimBadRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScimFilterParserTest {

    @Test
    fun `parse returns null for null input`() {
        assertNull(ScimFilterParser.parse(null))
    }

    @Test
    fun `parse returns null for blank input`() {
        assertNull(ScimFilterParser.parse(""))
        assertNull(ScimFilterParser.parse("   "))
    }

    @Test
    fun `parse eq filter`() {
        val filter = ScimFilterParser.parse("""userName eq "john"""")

        assertIs<EqFilter>(filter)
        assertEquals("userName", filter.attribute)
        assertEquals("john", filter.value)
    }

    @Test
    fun `parse eq filter with nested attribute`() {
        val filter = ScimFilterParser.parse("""emails.value eq "john@example.com"""")

        assertIs<EqFilter>(filter)
        assertEquals("emails.value", filter.attribute)
        assertEquals("john@example.com", filter.value)
    }

    @Test
    fun `parse co filter`() {
        val filter = ScimFilterParser.parse("""displayName co "John"""")

        assertIs<CoFilter>(filter)
        assertEquals("displayName", filter.attribute)
        assertEquals("John", filter.value)
    }

    @Test
    fun `parse sw filter`() {
        val filter = ScimFilterParser.parse("""userName sw "j"""")

        assertIs<SwFilter>(filter)
        assertEquals("userName", filter.attribute)
        assertEquals("j", filter.value)
    }

    @Test
    fun `parse and filter`() {
        val filter = ScimFilterParser.parse("""userName eq "john" and active eq "true"""")

        assertIs<AndFilter>(filter)
        assertIs<EqFilter>(filter.left)
        assertIs<EqFilter>(filter.right)
    }

    @Test
    fun `parse or filter`() {
        val filter = ScimFilterParser.parse("""userName eq "john" or userName eq "jane"""")

        assertIs<OrFilter>(filter)
        assertIs<EqFilter>(filter.left)
        assertIs<EqFilter>(filter.right)
    }

    @Test
    fun `parse case insensitive operator`() {
        val filter = ScimFilterParser.parse("""userName EQ "john"""")

        assertIs<EqFilter>(filter)
        assertEquals("userName", filter.attribute)
        assertEquals("john", filter.value)
    }

    @Test
    fun `parse throws for invalid filter syntax`() {
        assertFailsWith<ScimBadRequestException> {
            ScimFilterParser.parse("invalid filter")
        }
    }

    @Test
    fun `EqFilter matches string attribute`() {
        val filter = EqFilter("userName", "john")
        val attributes = mapOf<String, Any?>("userName" to "john")

        assertTrue(filter.matches(attributes))
    }

    @Test
    fun `EqFilter matches case insensitively`() {
        val filter = EqFilter("userName", "JOHN")
        val attributes = mapOf<String, Any?>("userName" to "john")

        assertTrue(filter.matches(attributes))
    }

    @Test
    fun `EqFilter matches nested attribute in list`() {
        val filter = EqFilter("emails.value", "john@example.com")
        val attributes = mapOf<String, Any?>(
            "emails" to listOf(
                mapOf("value" to "john@example.com", "primary" to true)
            )
        )

        assertTrue(filter.matches(attributes))
    }

    @Test
    fun `CoFilter matches substring`() {
        val filter = CoFilter("displayName", "ohn")
        val attributes = mapOf<String, Any?>("displayName" to "John Doe")

        assertTrue(filter.matches(attributes))
    }

    @Test
    fun `SwFilter matches prefix`() {
        val filter = SwFilter("userName", "joh")
        val attributes = mapOf<String, Any?>("userName" to "john")

        assertTrue(filter.matches(attributes))
    }
}

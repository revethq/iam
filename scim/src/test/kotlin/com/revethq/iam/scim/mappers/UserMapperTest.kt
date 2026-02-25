package com.revethq.iam.scim.mappers

import com.revethq.core.Metadata
import com.revethq.iam.scim.dtos.ScimEmail
import com.revethq.iam.scim.dtos.ScimUser
import com.revethq.iam.user.domain.User
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserMapperTest {
    private val baseUrl = "https://example.com"

    @Test
    fun `toScimUser maps basic fields`() {
        val userId = UUID.randomUUID()
        val now = OffsetDateTime.now()
        val user =
            User(
                id = userId,
                username = "john.doe",
                email = "john@example.com",
                metadata = Metadata(),
                createdOn = now,
                updatedOn = now,
            )

        val scimUser = user.toScimUser(baseUrl)

        assertEquals(userId.toString(), scimUser.id)
        assertEquals("john.doe", scimUser.userName)
        assertNotNull(scimUser.emails)
        assertEquals(1, scimUser.emails!!.size)
        assertEquals("john@example.com", scimUser.emails!![0].value)
        assertTrue(scimUser.emails!![0].primary)
        assertEquals("work", scimUser.emails!![0].type)
    }

    @Test
    fun `toScimUser includes meta`() {
        val userId = UUID.randomUUID()
        val user =
            User(
                id = userId,
                username = "john.doe",
                email = "john@example.com",
            )

        val scimUser = user.toScimUser(baseUrl)

        assertNotNull(scimUser.meta)
        assertEquals("User", scimUser.meta!!.resourceType)
        assertEquals("$baseUrl/scim/v2/Users/$userId", scimUser.meta!!.location)
    }

    @Test
    fun `toScimUser maps properties to SCIM attributes`() {
        val user =
            User(
                id = UUID.randomUUID(),
                username = "john.doe",
                email = "john@example.com",
                metadata =
                    Metadata(
                        properties =
                            mapOf(
                                "displayName" to "John Doe",
                                "active" to false,
                                "locale" to "en-US",
                            ),
                    ),
            )

        val scimUser = user.toScimUser(baseUrl, "ext-123")

        assertEquals("ext-123", scimUser.externalId)
        assertEquals("John Doe", scimUser.displayName)
        assertEquals(false, scimUser.active)
        assertEquals("en-US", scimUser.locale)
    }

    @Test
    fun `toDomain creates User from ScimUser`() {
        val scimUser =
            ScimUser(
                userName = "jane.doe",
                emails =
                    listOf(
                        ScimEmail(value = "jane@example.com", primary = true),
                    ),
                externalId = "ext-456",
                displayName = "Jane Doe",
                active = true,
                locale = "en-GB",
            )

        val user = scimUser.toDomain()

        assertEquals("jane.doe", user.username)
        assertEquals("jane@example.com", user.email)
        // externalId is handled via IdentityProviderLink, not stored in metadata
        assertEquals("Jane Doe", user.metadata.properties?.get("displayName"))
        assertEquals(true, user.metadata.properties?.get("active"))
        assertEquals("en-GB", user.metadata.properties?.get("locale"))
    }

    @Test
    fun `toDomain uses primary email`() {
        val scimUser =
            ScimUser(
                userName = "test",
                emails =
                    listOf(
                        ScimEmail(value = "secondary@example.com", primary = false),
                        ScimEmail(value = "primary@example.com", primary = true),
                    ),
            )

        val user = scimUser.toDomain()

        assertEquals("primary@example.com", user.email)
    }

    @Test
    fun `toDomain uses first email if no primary`() {
        val scimUser =
            ScimUser(
                userName = "test",
                emails =
                    listOf(
                        ScimEmail(value = "first@example.com", primary = false),
                        ScimEmail(value = "second@example.com", primary = false),
                    ),
            )

        val user = scimUser.toDomain()

        assertEquals("first@example.com", user.email)
    }

    @Test
    fun `toDomain throws if no emails`() {
        val scimUser =
            ScimUser(
                userName = "test",
                emails = null,
            )

        assertFailsWith<IllegalArgumentException> {
            scimUser.toDomain()
        }
    }

    @Test
    fun `toDomain preserves id if provided`() {
        val existingId = UUID.randomUUID()
        val scimUser =
            ScimUser(
                id = existingId.toString(),
                userName = "test",
                emails = listOf(ScimEmail(value = "test@example.com", primary = true)),
            )

        val user = scimUser.toDomain()

        assertEquals(existingId, user.id)
    }

    @Test
    fun `updateDomain updates existing user`() {
        val existingUser =
            User(
                id = UUID.randomUUID(),
                username = "old.name",
                email = "old@example.com",
                metadata =
                    Metadata(
                        properties = mapOf("existingProp" to "value"),
                    ),
            )

        val scimUser =
            ScimUser(
                userName = "new.name",
                emails = listOf(ScimEmail(value = "new@example.com", primary = true)),
                displayName = "New Name",
            )

        val updatedUser = scimUser.updateDomain(existingUser)

        assertEquals(existingUser.id, updatedUser.id)
        assertEquals("new.name", updatedUser.username)
        assertEquals("new@example.com", updatedUser.email)
        assertEquals("New Name", updatedUser.metadata.properties?.get("displayName"))
    }
}

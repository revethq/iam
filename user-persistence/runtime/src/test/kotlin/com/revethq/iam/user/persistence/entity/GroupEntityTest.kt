package com.revethq.iam.user.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.Group
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GroupEntityTest {
    @Test
    fun `toDomain maps all fields correctly`() {
        val entity =
            GroupEntity().apply {
                id = UUID.randomUUID()
                displayName = "Engineering"
                externalId = "ext-group-123"
                metadata = Metadata(properties = mapOf("key" to "value"))
                createdOn = OffsetDateTime.now().minusDays(1)
                updatedOn = OffsetDateTime.now()
            }

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.displayName, domain.displayName)
        assertEquals(entity.externalId, domain.externalId)
        assertEquals(entity.metadata, domain.metadata)
        assertEquals(entity.createdOn, domain.createdOn)
        assertEquals(entity.updatedOn, domain.updatedOn)
    }

    @Test
    fun `toDomain handles null externalId`() {
        val entity =
            GroupEntity().apply {
                id = UUID.randomUUID()
                displayName = "Marketing"
                externalId = null
                metadata = Metadata()
                createdOn = OffsetDateTime.now()
                updatedOn = OffsetDateTime.now()
            }

        val domain = entity.toDomain()

        assertNull(domain.externalId)
    }

    @Test
    fun `fromDomain maps all fields correctly`() {
        val now = OffsetDateTime.now()
        val group =
            Group(
                id = UUID.randomUUID(),
                displayName = "Sales",
                externalId = "ext-sales-456",
                metadata = Metadata(properties = mapOf("department" to "revenue")),
                createdOn = now.minusHours(2),
                updatedOn = now,
            )

        val entity = GroupEntity.fromDomain(group)

        assertEquals(group.id, entity.id)
        assertEquals(group.displayName, entity.displayName)
        assertEquals(group.externalId, entity.externalId)
        assertEquals(group.metadata, entity.metadata)
        assertEquals(group.createdOn, entity.createdOn)
        assertEquals(group.updatedOn, entity.updatedOn)
    }

    @Test
    fun `fromDomain sets timestamps when null`() {
        val group =
            Group(
                id = UUID.randomUUID(),
                displayName = "Support",
                createdOn = null,
                updatedOn = null,
            )

        val entity = GroupEntity.fromDomain(group)

        assertNotNull(entity.createdOn)
        assertNotNull(entity.updatedOn)
    }

    @Test
    fun `fromDomain handles null externalId`() {
        val group =
            Group(
                id = UUID.randomUUID(),
                displayName = "HR",
                externalId = null,
            )

        val entity = GroupEntity.fromDomain(group)

        assertNull(entity.externalId)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original =
            Group(
                id = UUID.randomUUID(),
                displayName = "DevOps",
                externalId = "devops-team",
                metadata = Metadata(properties = mapOf("oncall" to "true")),
                createdOn = OffsetDateTime.now().minusDays(30),
                updatedOn = OffsetDateTime.now(),
            )

        val entity = GroupEntity.fromDomain(original)
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.displayName, restored.displayName)
        assertEquals(original.externalId, restored.externalId)
        assertEquals(original.metadata, restored.metadata)
        assertEquals(original.createdOn, restored.createdOn)
        assertEquals(original.updatedOn, restored.updatedOn)
    }
}

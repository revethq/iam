package com.revethq.iam.serviceaccount.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.serviceaccount.domain.ServiceAccount
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ServiceAccountEntityTest {

    @Test
    fun `toDomain converts entity to domain`() {
        val entity = ServiceAccountEntity().apply {
            id = UUID.randomUUID()
            name = "my-service"
            description = "A test service account"
            tenantId = "tenant-1"
            metadata = Metadata()
            createdOn = OffsetDateTime.now()
            updatedOn = OffsetDateTime.now()
        }

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.description, domain.description)
        assertEquals(entity.tenantId, domain.tenantId)
        assertEquals(entity.createdOn, domain.createdOn)
        assertEquals(entity.updatedOn, domain.updatedOn)
    }

    @Test
    fun `fromDomain converts domain to entity`() {
        val domain = ServiceAccount(
            id = UUID.randomUUID(),
            name = "my-service",
            description = "A test service account",
            tenantId = "tenant-1"
        )

        val entity = ServiceAccountEntity.fromDomain(domain)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.description, entity.description)
        assertEquals(domain.tenantId, entity.tenantId)
        assertNotNull(entity.createdOn)
        assertNotNull(entity.updatedOn)
    }

    @Test
    fun `fromDomain handles null optional fields`() {
        val domain = ServiceAccount(
            id = UUID.randomUUID(),
            name = "minimal-service"
        )

        val entity = ServiceAccountEntity.fromDomain(domain)

        assertEquals(domain.name, entity.name)
        assertNull(entity.description)
        assertNull(entity.tenantId)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = ServiceAccount(
            id = UUID.randomUUID(),
            name = "roundtrip-service",
            description = "test description",
            tenantId = "tenant-2",
            metadata = Metadata(),
            createdOn = OffsetDateTime.now(),
            updatedOn = OffsetDateTime.now()
        )

        val entity = ServiceAccountEntity.fromDomain(original)
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.description, restored.description)
        assertEquals(original.tenantId, restored.tenantId)
    }
}

package com.revethq.iam.serviceaccount.persistence.service

import com.revethq.iam.serviceaccount.domain.ServiceAccount
import com.revethq.iam.serviceaccount.persistence.entity.ServiceAccountEntity
import com.revethq.iam.serviceaccount.persistence.repository.ServiceAccountRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServiceAccountServiceImplTest {

    private val repository = mockk<ServiceAccountRepository>(relaxed = true)
    private val service = ServiceAccountServiceImpl(repository)

    @Test
    fun `create persists entity and returns domain`() {
        val sa = ServiceAccount(
            id = UUID.randomUUID(),
            name = "test-sa",
            description = "desc",
            tenantId = "t1"
        )

        val entitySlot = slot<ServiceAccountEntity>()
        every { repository.persist(capture(entitySlot)) } answers { }

        val result = service.create(sa)

        verify { repository.persist(any<ServiceAccountEntity>()) }
        assertEquals(sa.id, result.id)
        assertEquals(sa.name, result.name)
        assertEquals(sa.description, result.description)
        assertEquals(sa.tenantId, result.tenantId)
        assertNotNull(result.createdOn)
        assertNotNull(result.updatedOn)
    }

    @Test
    fun `findById returns domain when found`() {
        val id = UUID.randomUUID()
        val entity = ServiceAccountEntity.fromDomain(
            ServiceAccount(id = id, name = "found-sa")
        )
        every { repository.findById(id) } returns entity

        val result = service.findById(id)

        assertNotNull(result)
        assertEquals(id, result.id)
        assertEquals("found-sa", result.name)
    }

    @Test
    fun `findById returns null when not found`() {
        val id = UUID.randomUUID()
        every { repository.findById(id) } returns null

        val result = service.findById(id)

        assertNull(result)
    }

    @Test
    fun `delete returns true when entity exists`() {
        val id = UUID.randomUUID()
        every { repository.deleteById(id) } returns true

        assertTrue(service.delete(id))
    }

    @Test
    fun `delete returns false when entity does not exist`() {
        val id = UUID.randomUUID()
        every { repository.deleteById(id) } returns false

        assertFalse(service.delete(id))
    }

    @Test
    fun `count delegates to repository`() {
        every { repository.count() } returns 42L

        assertEquals(42L, service.count())
    }
}

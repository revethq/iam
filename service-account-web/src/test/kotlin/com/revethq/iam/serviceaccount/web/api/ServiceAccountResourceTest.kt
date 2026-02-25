package com.revethq.iam.serviceaccount.web.api

import com.revethq.iam.serviceaccount.domain.ServiceAccount
import com.revethq.iam.serviceaccount.persistence.Page
import com.revethq.iam.serviceaccount.persistence.service.ServiceAccountService
import com.revethq.iam.serviceaccount.web.dto.CreateServiceAccountRequest
import com.revethq.iam.serviceaccount.web.dto.UpdateServiceAccountRequest
import com.revethq.iam.serviceaccount.web.exception.ServiceAccountNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServiceAccountResourceTest {
    private val serviceAccountService = mockk<ServiceAccountService>(relaxed = true)

    private val resource =
        ServiceAccountResource().apply {
            this.serviceAccountService = this@ServiceAccountResourceTest.serviceAccountService
        }

    @Test
    fun `createServiceAccount returns 201 with created service account`() {
        val request =
            CreateServiceAccountRequest(
                name = "ci-bot",
                description = "CI pipeline",
                tenantId = "acme",
            )

        val saSlot = slot<ServiceAccount>()
        every { serviceAccountService.create(capture(saSlot)) } answers { saSlot.captured }

        val response = resource.createServiceAccount(request)

        assertEquals(201, response.status)
        verify { serviceAccountService.create(any()) }
    }

    @Test
    fun `getServiceAccount returns service account when found`() {
        val sa = createServiceAccount("ci-bot")
        every { serviceAccountService.findById(sa.id) } returns sa

        val result = resource.getServiceAccount(sa.id.toString())

        assertEquals(sa.name, result.name)
        assertEquals(sa.description, result.description)
        assertEquals(sa.tenantId, result.tenantId)
    }

    @Test
    fun `getServiceAccount throws not found when missing`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.findById(id) } returns null

        assertFailsWith<ServiceAccountNotFoundException> {
            resource.getServiceAccount(id.toString())
        }
    }

    @Test
    fun `listServiceAccounts returns paginated results`() {
        val accounts = listOf(createServiceAccount("sa1"), createServiceAccount("sa2"))
        val page = Page(accounts, 2, 0, 20)

        every { serviceAccountService.list(0, 21) } returns page

        val result = resource.listServiceAccounts(0, 20)

        assertEquals(2, result.content.size)
        assertEquals(0, result.page)
        assertEquals(20, result.size)
        assertFalse(result.hasMore)
    }

    @Test
    fun `listServiceAccounts indicates hasMore when more results exist`() {
        val accounts = (1..21).map { createServiceAccount("sa$it") }
        val page = Page(accounts, 21, 0, 21)

        every { serviceAccountService.list(0, 21) } returns page

        val result = resource.listServiceAccounts(0, 20)

        assertEquals(20, result.content.size)
        assertTrue(result.hasMore)
    }

    @Test
    fun `updateServiceAccount updates existing service account`() {
        val existing = createServiceAccount("old-name")
        every { serviceAccountService.findById(existing.id) } returns existing

        val request =
            UpdateServiceAccountRequest(
                name = "new-name",
                description = "updated desc",
                tenantId = "new-tenant",
            )

        val saSlot = slot<ServiceAccount>()
        every { serviceAccountService.update(capture(saSlot)) } answers { saSlot.captured }

        val result = resource.updateServiceAccount(existing.id.toString(), request)

        assertEquals("new-name", result.name)
        assertEquals("updated desc", result.description)
        assertEquals("new-tenant", result.tenantId)
    }

    @Test
    fun `updateServiceAccount throws not found when missing`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.findById(id) } returns null

        val request = UpdateServiceAccountRequest(name = "name")

        assertFailsWith<ServiceAccountNotFoundException> {
            resource.updateServiceAccount(id.toString(), request)
        }
    }

    @Test
    fun `deleteServiceAccount returns 204 on success`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.delete(id) } returns true

        val response = resource.deleteServiceAccount(id.toString())

        assertEquals(204, response.status)
    }

    @Test
    fun `deleteServiceAccount throws not found when missing`() {
        val id = UUID.randomUUID()
        every { serviceAccountService.delete(id) } returns false

        assertFailsWith<ServiceAccountNotFoundException> {
            resource.deleteServiceAccount(id.toString())
        }
    }

    private fun createServiceAccount(
        name: String,
        id: UUID = UUID.randomUUID(),
    ): ServiceAccount =
        ServiceAccount(
            id = id,
            name = name,
            description = "$name description",
            tenantId = "test-tenant",
        )
}

package com.revethq.iam.permission.web.api

import com.revethq.iam.permission.discovery.PermissionDeclaration
import com.revethq.iam.permission.discovery.PermissionManifest
import com.revethq.iam.permission.discovery.PermissionRegistry
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WellKnownPermissionsResourceTest {
    private val permissionRegistry = mockk<PermissionRegistry>()

    private val resource =
        WellKnownPermissionsResource().apply {
            this.permissionRegistry = this@WellKnownPermissionsResourceTest.permissionRegistry
        }

    @Test
    fun `returns aggregated permissions from multiple providers`() {
        every { permissionRegistry.allManifests() } returns
            listOf(
                PermissionManifest(
                    service = "iam",
                    permissions =
                        listOf(
                            PermissionDeclaration("iam:CreateUser", "Create a user", "urn:revet:iam:{tenantId}:user/{userId}"),
                            PermissionDeclaration("iam:GetUser", "Get a user", "urn:revet:iam:{tenantId}:user/{userId}"),
                        ),
                ),
                PermissionManifest(
                    service = "billing",
                    permissions =
                        listOf(
                            PermissionDeclaration(
                                "billing:CreateInvoice",
                                "Create an invoice",
                                "urn:revet:billing:{tenantId}:invoice/{invoiceId}",
                            ),
                        ),
                ),
            )

        val response = resource.getPermissions()

        assertEquals(2, response.manifests.size)

        val iamManifest = response.manifests[0]
        assertEquals("iam", iamManifest.service)
        assertEquals(2, iamManifest.permissions.size)
        assertEquals("iam:CreateUser", iamManifest.permissions[0].action)
        assertEquals("Create a user", iamManifest.permissions[0].description)
        assertEquals("urn:revet:iam:{tenantId}:user/{userId}", iamManifest.permissions[0].resourceType)
        assertEquals("iam:GetUser", iamManifest.permissions[1].action)

        val billingManifest = response.manifests[1]
        assertEquals("billing", billingManifest.service)
        assertEquals(1, billingManifest.permissions.size)
        assertEquals("billing:CreateInvoice", billingManifest.permissions[0].action)
    }

    @Test
    fun `returns empty manifests when no providers registered`() {
        every { permissionRegistry.allManifests() } returns emptyList()

        val response = resource.getPermissions()

        assertTrue(response.manifests.isEmpty())
    }

    @Test
    fun `includes permissions with null optional fields`() {
        every { permissionRegistry.allManifests() } returns
            listOf(
                PermissionManifest(
                    service = "iam",
                    permissions =
                        listOf(
                            PermissionDeclaration("iam:CreateUser"),
                        ),
                ),
            )

        val response = resource.getPermissions()

        assertEquals(1, response.manifests.size)
        val permission = response.manifests[0].permissions[0]
        assertEquals("iam:CreateUser", permission.action)
        assertEquals(null, permission.description)
        assertEquals(null, permission.resourceType)
    }
}

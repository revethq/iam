package com.revethq.iam.permission.discovery

import io.mockk.every
import io.mockk.mockk
import jakarta.enterprise.inject.Instance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PermissionRegistryTest {
    @Test
    fun `aggregates manifests from multiple providers`() {
        val provider1 =
            mockk<PermissionProvider> {
                every { manifest() } returns
                    PermissionManifest(
                        service = "iam",
                        permissions =
                            listOf(
                                PermissionDeclaration("iam:CreateUser", "Create a user"),
                            ),
                    )
            }
        val provider2 =
            mockk<PermissionProvider> {
                every { manifest() } returns
                    PermissionManifest(
                        service = "billing",
                        permissions =
                            listOf(
                                PermissionDeclaration("billing:CreateInvoice", "Create an invoice"),
                            ),
                    )
            }

        val instance =
            mockk<Instance<PermissionProvider>> {
                every { iterator() } returns mutableListOf(provider1, provider2).iterator()
            }
        val registry = PermissionRegistry(instance)

        val manifests = registry.allManifests()
        assertEquals(2, manifests.size)
        assertEquals("iam", manifests[0].service)
        assertEquals("billing", manifests[1].service)
    }

    @Test
    fun `returns empty list when no providers exist`() {
        val instance =
            mockk<Instance<PermissionProvider>> {
                every { iterator() } returns mutableListOf<PermissionProvider>().iterator()
            }
        val registry = PermissionRegistry(instance)

        val manifests = registry.allManifests()
        assertTrue(manifests.isEmpty())
    }

    @Test
    fun `allPermissions flattens declarations from all manifests`() {
        val provider1 =
            mockk<PermissionProvider> {
                every { manifest() } returns
                    PermissionManifest(
                        service = "iam",
                        permissions =
                            listOf(
                                PermissionDeclaration("iam:CreateUser", "Create a user"),
                                PermissionDeclaration("iam:GetUser", "Get a user"),
                            ),
                    )
            }
        val provider2 =
            mockk<PermissionProvider> {
                every { manifest() } returns
                    PermissionManifest(
                        service = "billing",
                        permissions =
                            listOf(
                                PermissionDeclaration("billing:CreateInvoice", "Create an invoice"),
                            ),
                    )
            }

        val instance =
            mockk<Instance<PermissionProvider>> {
                every { iterator() } returns mutableListOf(provider1, provider2).iterator()
            }
        val registry = PermissionRegistry(instance)

        val permissions = registry.allPermissions()
        assertEquals(3, permissions.size)
        assertEquals("iam:CreateUser", permissions[0].action)
        assertEquals("iam:GetUser", permissions[1].action)
        assertEquals("billing:CreateInvoice", permissions[2].action)
    }
}

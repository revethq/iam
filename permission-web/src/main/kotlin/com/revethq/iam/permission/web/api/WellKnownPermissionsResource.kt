package com.revethq.iam.permission.web.api

import com.revethq.iam.permission.discovery.PermissionRegistry
import com.revethq.iam.permission.web.dto.PermissionDeclarationDto
import com.revethq.iam.permission.web.dto.ServiceManifestDto
import com.revethq.iam.permission.web.dto.WellKnownPermissionsResponse
import io.quarkus.arc.properties.UnlessBuildProperty
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@UnlessBuildProperty(name = "revet.iam.resources.disabled", stringValue = "true", enableIfMissing = true)
@Path("/.well-known/revet-permissions")
@Produces(MediaType.APPLICATION_JSON)
class WellKnownPermissionsResource {
    @Inject
    lateinit var permissionRegistry: PermissionRegistry

    @GET
    fun getPermissions(): WellKnownPermissionsResponse {
        val manifests =
            permissionRegistry.allManifests().map { manifest ->
                ServiceManifestDto(
                    service = manifest.service,
                    permissions =
                        manifest.permissions.map { p ->
                            PermissionDeclarationDto(
                                action = p.action,
                                description = p.description,
                                resourceType = p.resourceType,
                            )
                        },
                )
            }
        return WellKnownPermissionsResponse(manifests = manifests)
    }
}

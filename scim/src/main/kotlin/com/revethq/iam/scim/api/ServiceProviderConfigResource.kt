package com.revethq.iam.scim.api

import io.quarkus.runtime.annotations.RegisterForReflection
import com.revethq.iam.scim.dtos.ScimMeta
import com.revethq.iam.scim.dtos.ServiceProviderConfig
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.UriInfo
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@ScimEndpoint
@RegisterForReflection
@Path("/scim/v2/ServiceProviderConfig")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SCIM Discovery", description = "SCIM 2.0 discovery endpoints")
open class ServiceProviderConfigResource {

    @Context
    protected lateinit var uriInfo: UriInfo

    protected val baseUrl: String
        get() = uriInfo.baseUri.toString().removeSuffix("/")

    @GET
    @Operation(
        summary = "Get service provider configuration",
        description = "Returns the SCIM service provider configuration including supported features"
    )
    @APIResponse(
        responseCode = "200",
        description = "Service provider configuration",
        content = [Content(schema = Schema(implementation = ServiceProviderConfig::class))]
    )
    open fun getServiceProviderConfig(): ServiceProviderConfig {
        return ServiceProviderConfig(
            meta = ScimMeta(
                resourceType = "ServiceProviderConfig",
                location = "$baseUrl/scim/v2/ServiceProviderConfig"
            )
        )
    }
}

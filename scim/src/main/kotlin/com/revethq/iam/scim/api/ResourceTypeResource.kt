package com.revethq.iam.scim.api

import com.revethq.iam.scim.dtos.ScimListResponse
import com.revethq.iam.scim.dtos.ScimMeta
import com.revethq.iam.scim.dtos.ScimResourceType
import com.revethq.iam.scim.exception.ScimNotFoundException
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.UriInfo
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/scim/v2/ResourceTypes")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SCIM Discovery", description = "SCIM 2.0 discovery endpoints")
open class ResourceTypeResource {

    @Context
    protected lateinit var uriInfo: UriInfo

    protected val baseUrl: String
        get() = uriInfo.baseUri.toString().removeSuffix("/")

    @GET
    @Operation(summary = "List resource types", description = "Returns all supported SCIM resource types")
    @APIResponse(
        responseCode = "200",
        description = "List of resource types"
    )
    open fun listResourceTypes(): ScimListResponse<ScimResourceType> {
        val resourceTypes = listOf(getUserResourceType(), getGroupResourceType())
        return ScimListResponse(
            totalResults = resourceTypes.size,
            startIndex = 1,
            itemsPerPage = resourceTypes.size,
            resources = resourceTypes
        )
    }

    @GET
    @Path("/{name}")
    @Operation(summary = "Get resource type", description = "Get a SCIM resource type by name")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Resource type found",
            content = [Content(schema = Schema(implementation = ScimResourceType::class))]
        ),
        APIResponse(responseCode = "404", description = "Resource type not found")
    )
    open fun getResourceType(
        @Parameter(description = "Resource type name", required = true)
        @PathParam("name")
        name: String
    ): ScimResourceType {
        return when (name.lowercase()) {
            "user" -> getUserResourceType()
            "group" -> getGroupResourceType()
            else -> throw ScimNotFoundException("ResourceType", name)
        }
    }

    private fun getUserResourceType(): ScimResourceType {
        return ScimResourceType(
            id = "User",
            name = "User",
            endpoint = "/Users",
            description = "User Account",
            schema = "urn:ietf:params:scim:schemas:core:2.0:User",
            meta = ScimMeta(
                resourceType = "ResourceType",
                location = "$baseUrl/scim/v2/ResourceTypes/User"
            )
        )
    }

    private fun getGroupResourceType(): ScimResourceType {
        return ScimResourceType(
            id = "Group",
            name = "Group",
            endpoint = "/Groups",
            description = "Group",
            schema = "urn:ietf:params:scim:schemas:core:2.0:Group",
            meta = ScimMeta(
                resourceType = "ResourceType",
                location = "$baseUrl/scim/v2/ResourceTypes/Group"
            )
        )
    }
}

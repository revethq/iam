package com.revethq.iam.scim.api

import com.revethq.iam.scim.dtos.ScimListResponse
import com.revethq.iam.scim.dtos.ScimMeta
import com.revethq.iam.scim.dtos.ScimSchema
import com.revethq.iam.scim.dtos.ScimSchemaAttribute
import com.revethq.iam.scim.exception.ScimNotFoundException
import io.quarkus.runtime.annotations.RegisterForReflection
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

@ScimEndpoint
@RegisterForReflection
@Path("/scim/v2/Schemas")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SCIM Discovery", description = "SCIM 2.0 discovery endpoints")
open class SchemaResource {
    @Context
    protected lateinit var uriInfo: UriInfo

    protected val baseUrl: String
        get() = uriInfo.baseUri.toString().removeSuffix("/")

    @GET
    @Operation(summary = "List schemas", description = "Returns all supported SCIM schemas")
    @APIResponse(
        responseCode = "200",
        description = "List of schemas",
    )
    open fun listSchemas(): ScimListResponse<ScimSchema> {
        val schemas = listOf(getUserSchema(), getGroupSchema())
        return ScimListResponse(
            totalResults = schemas.size,
            startIndex = 1,
            itemsPerPage = schemas.size,
            resources = schemas,
        )
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get schema", description = "Get a SCIM schema by ID (URN)")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Schema found",
            content = [Content(schema = Schema(implementation = ScimSchema::class))],
        ),
        APIResponse(responseCode = "404", description = "Schema not found"),
    )
    open fun getSchema(
        @Parameter(description = "Schema ID (URN)", required = true)
        @PathParam("id")
        id: String,
    ): ScimSchema =
        when (id) {
            USER_SCHEMA_ID -> getUserSchema()
            GROUP_SCHEMA_ID -> getGroupSchema()
            else -> throw ScimNotFoundException("Schema", id)
        }

    private fun getUserSchema(): ScimSchema =
        ScimSchema(
            id = USER_SCHEMA_ID,
            name = "User",
            description = "User Account",
            attributes =
                listOf(
                    ScimSchemaAttribute(
                        name = "userName",
                        type = "string",
                        description = "Unique identifier for the User",
                        required = true,
                        caseExact = false,
                        mutability = "readWrite",
                        uniqueness = "server",
                    ),
                    ScimSchemaAttribute(
                        name = "name",
                        type = "complex",
                        description = "The components of the user's name",
                        subAttributes =
                            listOf(
                                ScimSchemaAttribute(name = "formatted", type = "string", description = "Full name"),
                                ScimSchemaAttribute(name = "familyName", type = "string", description = "Family name"),
                                ScimSchemaAttribute(name = "givenName", type = "string", description = "Given name"),
                                ScimSchemaAttribute(name = "middleName", type = "string", description = "Middle name"),
                            ),
                    ),
                    ScimSchemaAttribute(
                        name = "displayName",
                        type = "string",
                        description = "Display name for the User",
                    ),
                    ScimSchemaAttribute(
                        name = "emails",
                        type = "complex",
                        multiValued = true,
                        description = "Email addresses for the User",
                        subAttributes =
                            listOf(
                                ScimSchemaAttribute(name = "value", type = "string", description = "Email address"),
                                ScimSchemaAttribute(name = "type", type = "string", description = "Type of email"),
                                ScimSchemaAttribute(name = "primary", type = "boolean", description = "Primary email indicator"),
                            ),
                    ),
                    ScimSchemaAttribute(
                        name = "active",
                        type = "boolean",
                        description = "User's administrative status",
                    ),
                    ScimSchemaAttribute(
                        name = "locale",
                        type = "string",
                        description = "User's locale",
                    ),
                ),
            meta =
                ScimMeta(
                    resourceType = "Schema",
                    location = "$baseUrl/scim/v2/Schemas/$USER_SCHEMA_ID",
                ),
        )

    private fun getGroupSchema(): ScimSchema =
        ScimSchema(
            id = GROUP_SCHEMA_ID,
            name = "Group",
            description = "Group",
            attributes =
                listOf(
                    ScimSchemaAttribute(
                        name = "displayName",
                        type = "string",
                        description = "Display name for the Group",
                        required = true,
                    ),
                    ScimSchemaAttribute(
                        name = "members",
                        type = "complex",
                        multiValued = true,
                        description = "A list of members of the Group",
                        subAttributes =
                            listOf(
                                ScimSchemaAttribute(
                                    name = "value",
                                    type = "string",
                                    description = "Identifier of the member",
                                    mutability = "immutable",
                                ),
                                ScimSchemaAttribute(
                                    name = "display",
                                    type = "string",
                                    description = "Display name of the member",
                                    mutability = "readOnly",
                                ),
                                ScimSchemaAttribute(
                                    name = "type",
                                    type = "string",
                                    description = "Type of member (User or Group)",
                                    canonicalValues = listOf("User", "Group"),
                                ),
                                ScimSchemaAttribute(
                                    name = "\$ref",
                                    type = "reference",
                                    description = "URI of the member resource",
                                    mutability = "readOnly",
                                    referenceTypes = listOf("User", "Group"),
                                ),
                            ),
                    ),
                ),
            meta =
                ScimMeta(
                    resourceType = "Schema",
                    location = "$baseUrl/scim/v2/Schemas/$GROUP_SCHEMA_ID",
                ),
        )

    companion object {
        const val USER_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:User"
        const val GROUP_SCHEMA_ID = "urn:ietf:params:scim:schemas:core:2.0:Group"
    }
}

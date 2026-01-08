## Context

This design covers the SCIM v2 API implementation for the Revet IAM library. SCIM (System for Cross-domain Identity Management) is an open standard for automating user provisioning. Our primary integration target is Okta, but the implementation follows RFC 7644 for broad compatibility.

**Stakeholders**: Developers integrating Revet IAM with enterprise IdPs (Okta, Azure AD, etc.)

**Constraints**:
- Must comply with SCIM 2.0 (RFC 7643, RFC 7644)
- Must pass Okta's SCIM provisioning validation
- JAX-RS for API layer (Quarkus compatibility)
- Eclipse MicroProfile OpenAPI for documentation

## Goals / Non-Goals

**Goals**:
- Full SCIM 2.0 compliance for User and Group resources
- Okta provisioning compatibility
- Clean separation: API (resources) → Mappers → Domain
- Extensible filtering and pagination

**Non-Goals**:
- Bulk operations endpoint (future enhancement)
- Nested group membership (users-only for now, designed for future extension)
- Custom schema extensions (standard SCIM schemas only)

## Decisions

### 1. Package Structure

```
scim/src/main/kotlin/com/revethq/iam/scim/
├── api/
│   ├── UserResource.kt
│   ├── GroupResource.kt
│   ├── ServiceProviderConfigResource.kt
│   ├── SchemaResource.kt
│   └── ResourceTypeResource.kt
├── dtos/
│   ├── ScimUser.kt
│   ├── ScimGroup.kt
│   ├── ScimListResponse.kt
│   ├── ScimError.kt
│   ├── ScimMeta.kt
│   ├── ScimName.kt
│   ├── ScimEmail.kt
│   ├── ScimMember.kt
│   ├── ScimPatchOp.kt
│   ├── ServiceProviderConfig.kt
│   ├── ScimSchema.kt
│   └── ScimResourceType.kt
└── mappers/
    ├── UserMapper.kt
    └── GroupMapper.kt
```

**Rationale**: Clear separation of concerns. Resources handle HTTP, DTOs handle serialization, mappers handle domain translation.

### 2. Domain Model Extensions

Add to `user` module:

```kotlin
// Group.kt
data class Group(
    var id: UUID,
    var displayName: String,
    var externalId: String? = null,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null
)

// GroupMember.kt
data class GroupMember(
    var id: UUID? = null,
    var groupId: UUID,
    var memberId: UUID,
    var memberType: MemberType = MemberType.USER,
    var createdOn: OffsetDateTime? = null
)

enum class MemberType { USER, GROUP }
```

**Rationale**: `memberType` field enables future nested group support without breaking changes.

### 3. SCIM DTOs

DTOs follow SCIM 2.0 schema strictly:

```kotlin
data class ScimUser(
    val schemas: List<String> = listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
    val id: String? = null,
    val externalId: String? = null,
    val meta: ScimMeta? = null,
    val userName: String,
    val name: ScimName? = null,
    val displayName: String? = null,
    val emails: List<ScimEmail>? = null,
    val active: Boolean = true,
    val locale: String? = null
)

data class ScimGroup(
    val schemas: List<String> = listOf("urn:ietf:params:scim:schemas:core:2.0:Group"),
    val id: String? = null,
    val externalId: String? = null,
    val meta: ScimMeta? = null,
    val displayName: String,
    val members: List<ScimMember>? = null
)

data class ScimListResponse<T>(
    val schemas: List<String> = listOf("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
    val totalResults: Int,
    val startIndex: Int,
    val itemsPerPage: Int,
    val Resources: List<T>
)

data class ScimError(
    val schemas: List<String> = listOf("urn:ietf:params:scim:api:messages:2.0:Error"),
    val status: String,
    val scimType: String? = null,
    val detail: String? = null
)
```

### 4. SCIM Filtering

Implement a simple filter parser supporting the `eq` operator (required by Okta):

```
filter=userName eq "john"
filter=externalId eq "abc123"
filter=emails.value eq "john@example.com"
```

**Approach**: Parse filter string into an AST, then convert to domain query criteria. Start with `eq` operator, extensible to `co`, `sw`, `gt`, `lt`, etc.

**Alternatives considered**:
- Full SCIM filter grammar parser: Over-engineered for Okta integration
- SQL-like string interpolation: Security risk (injection)

### 5. Error Handling

Implement JAX-RS `ExceptionMapper` for SCIM-compliant error responses:

```kotlin
@Provider
class ScimExceptionMapper : ExceptionMapper<ScimException> {
    override fun toResponse(e: ScimException): Response {
        return Response.status(e.status)
            .entity(ScimError(
                status = e.status.toString(),
                scimType = e.scimType,
                detail = e.message
            ))
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}
```

**scimType values** (per RFC 7644):
- `invalidFilter` - Bad filter syntax
- `invalidSyntax` - Malformed request
- `invalidValue` - Invalid attribute value
- `mutability` - Attempted to modify read-only attribute
- `uniqueness` - Duplicate value for unique attribute

### 6. Authentication

Bearer token authentication via JAX-RS filter:

```kotlin
@Provider
@Priority(Priorities.AUTHENTICATION)
class BearerTokenFilter : ContainerRequestFilter {
    override fun filter(ctx: ContainerRequestContext) {
        val auth = ctx.getHeaderString("Authorization")
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw ScimException(401, "unauthorized", "Missing or invalid bearer token")
        }
        // Token validation delegated to application
    }
}
```

**Rationale**: Simple filter allows applications to plug in their own token validation logic.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| SCIM filter injection | Parse into typed AST, never string interpolate |
| Okta validation failures | Test against Okta's SCIM validator tool |
| Performance with large result sets | Pagination required, add max page size later |
| Nested groups complexity | Deferred; `memberType` field provides forward compatibility |

## Migration Plan

No migration needed - this is a new capability. Existing `user` module consumers are unaffected.

## Open Questions

None - all requirements clarified.

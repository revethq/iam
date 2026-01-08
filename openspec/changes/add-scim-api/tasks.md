## 1. Domain Model

- [x] 1.1 Create `MemberType` enum in `user` module (`USER`, `GROUP`)
- [x] 1.2 Create `Group` domain class in `user` module
- [x] 1.3 Create `GroupMember` domain class in `user` module

## 2. SCIM DTOs

- [x] 2.1 Create `ScimMeta` DTO (resourceType, created, lastModified, location, version)
- [x] 2.2 Create `ScimName` DTO (formatted, familyName, givenName, middleName)
- [x] 2.3 Create `ScimEmail` DTO (value, type, primary)
- [x] 2.4 Create `ScimMember` DTO (value, display, type, $ref)
- [x] 2.5 Create `ScimUser` DTO with full SCIM schema
- [x] 2.6 Create `ScimGroup` DTO with full SCIM schema
- [x] 2.7 Create `ScimListResponse<T>` DTO for paginated results
- [x] 2.8 Create `ScimError` DTO for error responses
- [x] 2.9 Create `ScimPatchOp` DTO for PATCH operations
- [x] 2.10 Create `ServiceProviderConfig` DTO
- [x] 2.11 Create `ScimSchema` DTO for schema discovery
- [x] 2.12 Create `ScimResourceType` DTO for resource type discovery

## 3. Mappers

- [x] 3.1 Create `UserMapper` with `User.toScimUser()` extension and `ScimUser.toDomain()`
- [x] 3.2 Create `GroupMapper` with `Group.toScimGroup()` extension and `ScimGroup.toDomain()`

## 4. Error Handling

- [x] 4.1 Create `ScimException` with status, scimType, and detail
- [x] 4.2 Create `ScimExceptionMapper` JAX-RS provider

## 5. Authentication

- [x] 5.1 Create `BearerTokenFilter` JAX-RS ContainerRequestFilter

## 6. SCIM Filtering

- [x] 6.1 Create `ScimFilter` sealed class hierarchy (FilterExpression, EqFilter, etc.)
- [x] 6.2 Create `ScimFilterParser` to parse filter query param
- [x] 6.3 Integrate filter parsing into list endpoints

## 7. User Resource

- [x] 7.1 Create `UserResource` JAX-RS resource class with `@Path("/scim/v2/Users")`
- [x] 7.2 Implement `POST /Users` - create user
- [x] 7.3 Implement `GET /Users` - list users with pagination and filtering
- [x] 7.4 Implement `GET /Users/{id}` - get user by ID
- [x] 7.5 Implement `PUT /Users/{id}` - replace user
- [x] 7.6 Implement `PATCH /Users/{id}` - partial update user
- [x] 7.7 Implement `DELETE /Users/{id}` - delete user
- [x] 7.8 Add MicroProfile OpenAPI annotations to all endpoints

## 8. Group Resource

- [x] 8.1 Create `GroupResource` JAX-RS resource class with `@Path("/scim/v2/Groups")`
- [x] 8.2 Implement `POST /Groups` - create group
- [x] 8.3 Implement `GET /Groups` - list groups with pagination and filtering
- [x] 8.4 Implement `GET /Groups/{id}` - get group by ID
- [x] 8.5 Implement `PUT /Groups/{id}` - replace group
- [x] 8.6 Implement `PATCH /Groups/{id}` - partial update group (add/remove members)
- [x] 8.7 Implement `DELETE /Groups/{id}` - delete group
- [x] 8.8 Add MicroProfile OpenAPI annotations to all endpoints

## 9. Discovery Resources

- [x] 9.1 Create `ServiceProviderConfigResource` with `GET /ServiceProviderConfig`
- [x] 9.2 Create `SchemaResource` with `GET /Schemas` and `GET /Schemas/{id}`
- [x] 9.3 Create `ResourceTypeResource` with `GET /ResourceTypes` and `GET /ResourceTypes/{name}`
- [x] 9.4 Add MicroProfile OpenAPI annotations to all discovery endpoints

## 10. Build Configuration

- [x] 10.1 Add JAX-RS dependencies to `scim/build.gradle.kts`
- [x] 10.2 Add MicroProfile OpenAPI dependencies to `scim/build.gradle.kts`

## 11. Testing

- [x] 11.1 Write unit tests for `ScimFilterParser`
- [x] 11.2 Write unit tests for `UserMapper` and `GroupMapper`
- [ ] 11.3 Write integration tests for User endpoints
- [ ] 11.4 Write integration tests for Group endpoints
- [ ] 11.5 Write integration tests for Discovery endpoints
- [ ] 11.6 Validate against Okta SCIM test suite (manual)

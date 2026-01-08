## ADDED Requirements

### Requirement: SCIM User Management

The system SHALL provide SCIM 2.0 compliant endpoints for managing User resources at the `/scim/v2/Users` path.

#### Scenario: Create user
- **WHEN** a POST request is made to `/scim/v2/Users` with a valid ScimUser payload
- **THEN** the system creates the user and returns HTTP 201 with the created ScimUser including `id` and `meta`

#### Scenario: List users with pagination
- **WHEN** a GET request is made to `/scim/v2/Users` with optional `startIndex` and `count` parameters
- **THEN** the system returns a ScimListResponse containing users, `totalResults`, `startIndex`, and `itemsPerPage`

#### Scenario: Filter users
- **WHEN** a GET request is made to `/scim/v2/Users` with a `filter` parameter using `eq` operator
- **THEN** the system returns only users matching the filter criteria

#### Scenario: Get user by ID
- **WHEN** a GET request is made to `/scim/v2/Users/{id}` with a valid user ID
- **THEN** the system returns the ScimUser with HTTP 200

#### Scenario: Get user not found
- **WHEN** a GET request is made to `/scim/v2/Users/{id}` with a non-existent ID
- **THEN** the system returns HTTP 404 with a ScimError response

#### Scenario: Replace user
- **WHEN** a PUT request is made to `/scim/v2/Users/{id}` with a valid ScimUser payload
- **THEN** the system replaces the user and returns HTTP 200 with the updated ScimUser

#### Scenario: Partial update user
- **WHEN** a PATCH request is made to `/scim/v2/Users/{id}` with a ScimPatchOp payload
- **THEN** the system applies the patch operations and returns HTTP 200 with the updated ScimUser

#### Scenario: Deactivate user via PATCH
- **WHEN** a PATCH request sets `active` to `false` on a user
- **THEN** the system deactivates the user and returns HTTP 200

#### Scenario: Delete user
- **WHEN** a DELETE request is made to `/scim/v2/Users/{id}` with a valid user ID
- **THEN** the system deletes the user and returns HTTP 204

---

### Requirement: SCIM Group Management

The system SHALL provide SCIM 2.0 compliant endpoints for managing Group resources at the `/scim/v2/Groups` path.

#### Scenario: Create group
- **WHEN** a POST request is made to `/scim/v2/Groups` with a valid ScimGroup payload
- **THEN** the system creates the group and returns HTTP 201 with the created ScimGroup including `id` and `meta`

#### Scenario: List groups with pagination
- **WHEN** a GET request is made to `/scim/v2/Groups` with optional `startIndex` and `count` parameters
- **THEN** the system returns a ScimListResponse containing groups, `totalResults`, `startIndex`, and `itemsPerPage`

#### Scenario: Filter groups
- **WHEN** a GET request is made to `/scim/v2/Groups` with a `filter` parameter using `eq` operator
- **THEN** the system returns only groups matching the filter criteria

#### Scenario: Get group by ID
- **WHEN** a GET request is made to `/scim/v2/Groups/{id}` with a valid group ID
- **THEN** the system returns the ScimGroup with members list and HTTP 200

#### Scenario: Get group not found
- **WHEN** a GET request is made to `/scim/v2/Groups/{id}` with a non-existent ID
- **THEN** the system returns HTTP 404 with a ScimError response

#### Scenario: Replace group
- **WHEN** a PUT request is made to `/scim/v2/Groups/{id}` with a valid ScimGroup payload
- **THEN** the system replaces the group and its members and returns HTTP 200

#### Scenario: Add member to group via PATCH
- **WHEN** a PATCH request adds a member to a group using the `add` operation
- **THEN** the system adds the member and returns HTTP 200 with the updated ScimGroup

#### Scenario: Remove member from group via PATCH
- **WHEN** a PATCH request removes a member from a group using the `remove` operation
- **THEN** the system removes the member and returns HTTP 200 with the updated ScimGroup

#### Scenario: Delete group
- **WHEN** a DELETE request is made to `/scim/v2/Groups/{id}` with a valid group ID
- **THEN** the system deletes the group and returns HTTP 204

---

### Requirement: SCIM Service Provider Configuration

The system SHALL provide a SCIM 2.0 compliant endpoint for discovering server capabilities at `/scim/v2/ServiceProviderConfig`.

#### Scenario: Get service provider config
- **WHEN** a GET request is made to `/scim/v2/ServiceProviderConfig`
- **THEN** the system returns the ServiceProviderConfig indicating supported features (patch, filter, pagination)

---

### Requirement: SCIM Schema Discovery

The system SHALL provide a SCIM 2.0 compliant endpoint for discovering schemas at `/scim/v2/Schemas`.

#### Scenario: List schemas
- **WHEN** a GET request is made to `/scim/v2/Schemas`
- **THEN** the system returns a list of supported SCIM schemas (User, Group)

#### Scenario: Get schema by ID
- **WHEN** a GET request is made to `/scim/v2/Schemas/{id}` with a valid schema URN
- **THEN** the system returns the schema definition with attributes

---

### Requirement: SCIM Resource Type Discovery

The system SHALL provide a SCIM 2.0 compliant endpoint for discovering resource types at `/scim/v2/ResourceTypes`.

#### Scenario: List resource types
- **WHEN** a GET request is made to `/scim/v2/ResourceTypes`
- **THEN** the system returns a list of supported resource types (User, Group)

#### Scenario: Get resource type by name
- **WHEN** a GET request is made to `/scim/v2/ResourceTypes/{name}` with a valid resource type name
- **THEN** the system returns the resource type definition

---

### Requirement: SCIM Error Responses

The system SHALL return SCIM 2.0 compliant error responses per RFC 7644.

#### Scenario: Invalid filter syntax
- **WHEN** a request includes a malformed filter parameter
- **THEN** the system returns HTTP 400 with ScimError containing `scimType: "invalidFilter"`

#### Scenario: Invalid request syntax
- **WHEN** a request body cannot be parsed
- **THEN** the system returns HTTP 400 with ScimError containing `scimType: "invalidSyntax"`

#### Scenario: Uniqueness violation
- **WHEN** a request attempts to create a resource with a duplicate unique attribute
- **THEN** the system returns HTTP 409 with ScimError containing `scimType: "uniqueness"`

#### Scenario: Unauthorized request
- **WHEN** a request is made without a valid Bearer token
- **THEN** the system returns HTTP 401 with ScimError containing `scimType: "unauthorized"`

---

### Requirement: Bearer Token Authentication

The system SHALL require Bearer token authentication for all SCIM API endpoints.

#### Scenario: Valid bearer token
- **WHEN** a request includes a valid `Authorization: Bearer <token>` header
- **THEN** the request is processed normally

#### Scenario: Missing bearer token
- **WHEN** a request does not include an Authorization header
- **THEN** the system returns HTTP 401 with ScimError

#### Scenario: Invalid bearer token format
- **WHEN** a request includes an Authorization header that does not start with "Bearer "
- **THEN** the system returns HTTP 401 with ScimError

---

### Requirement: Group Domain Model

The system SHALL provide Group and GroupMember domain objects in the user module.

#### Scenario: Group with metadata
- **WHEN** a Group is created
- **THEN** it has `id`, `displayName`, `externalId`, `metadata`, `createdOn`, and `updatedOn` fields

#### Scenario: GroupMember with type
- **WHEN** a GroupMember is created
- **THEN** it has `groupId`, `memberId`, and `memberType` (defaulting to USER) fields

---

### Requirement: OpenAPI Documentation

The system SHALL annotate all SCIM API resources with Eclipse MicroProfile OpenAPI annotations.

#### Scenario: OpenAPI annotations present
- **WHEN** the OpenAPI specification is generated
- **THEN** all SCIM endpoints include operation summaries, descriptions, request/response schemas, and error responses

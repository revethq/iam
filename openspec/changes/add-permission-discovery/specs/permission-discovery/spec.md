## ADDED Requirements

### Requirement: Permission Declaration
Modules and downstream services SHALL be able to declare the permissions they define by implementing a `PermissionProvider` interface.

A permission declaration MUST have:
- An action string following the `{service}:{action}` format (e.g., `iam:CreateUser`)

A permission declaration MAY have:
- A human-readable description
- A resource type pattern showing the URN template for applicable resources

A permission manifest MUST have:
- A service name identifying the declaring service (e.g., `iam`, `billing`)
- A list of one or more permission declarations

#### Scenario: Module declares permissions
- **WHEN** a module implements `PermissionProvider` and returns a manifest with service "iam" and actions ["iam:CreateUser", "iam:GetUser"]
- **THEN** those permissions are available in the permission registry

#### Scenario: Permission with full metadata
- **WHEN** a permission is declared with action "iam:CreateUser", description "Create a new user", and resourceType "urn:revet:iam:{tenantId}:user/{userId}"
- **THEN** all metadata is preserved in the manifest

---

### Requirement: Permission Aggregation
The system SHALL aggregate permission declarations from all `PermissionProvider` implementations discovered on the classpath, including those from transitive library dependencies.

The aggregation MUST:
- Collect manifests from all `PermissionProvider` CDI beans
- Include providers from the downstream application itself
- Include providers from any library dependency that ships a `PermissionProvider` bean (discovered via Jandex + CDI)

#### Scenario: Aggregation from multiple providers
- **WHEN** the application depends on library A (declaring "iam:CreateUser") and library B (declaring "billing:CreateInvoice")
- **AND** the application itself declares "app:CustomAction"
- **THEN** the permission registry contains all three permissions grouped by their respective service names

#### Scenario: No providers registered
- **WHEN** no `PermissionProvider` beans exist on the classpath
- **THEN** the permission registry returns an empty list of manifests

---

### Requirement: Well-Known Permissions Endpoint
The system SHALL expose a `GET /.well-known/revet-permissions` endpoint that returns all aggregated permission declarations as JSON.

The response MUST:
- Contain a `manifests` array
- Group permissions by service name within each manifest entry
- Include the action, description, and resourceType for each permission declaration

The endpoint MUST NOT require authentication.

#### Scenario: Retrieve all permissions
- **WHEN** a GET request is made to `/.well-known/revet-permissions`
- **AND** two permission providers are registered (service "iam" with 2 permissions, service "billing" with 1 permission)
- **THEN** the response status is 200
- **AND** the response body contains a `manifests` array with 2 entries
- **AND** the "iam" manifest contains 2 permission declarations
- **AND** the "billing" manifest contains 1 permission declaration

#### Scenario: No permissions registered
- **WHEN** a GET request is made to `/.well-known/revet-permissions`
- **AND** no permission providers are registered
- **THEN** the response status is 200
- **AND** the response body contains an empty `manifests` array

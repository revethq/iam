# Design: Permission Discovery Endpoint

## Context

Downstream applications using `permission-web` enforce authorization via `@RequiresPermission` and `PolicyEvaluator`, but there is no mechanism to enumerate all permissions a running system understands. Admin UIs, CLI tooling, and service meshes need to discover available actions and resource types at runtime.

## Goals / Non-Goals

### Goals
- Allow any module or downstream service to declare its permissions statically via a CDI bean
- Aggregate all declared permissions (from the app and its transitive library dependencies) into a single endpoint
- Serve the aggregated permissions at `/.well-known/revet-permissions` as JSON
- Organize permissions by service name for clarity

### Non-Goals
- Auto-generating permission declarations from `@RequiresPermission` annotations (future enhancement)
- Persisting or caching permission declarations (they are compiled into the application)
- Permission versioning or change history
- Authentication/authorization on the well-known endpoint itself (it is informational metadata)

## Decisions

### Domain models live in `permission/` module

**Decision**: Place `PermissionDeclaration`, `PermissionManifest`, `PermissionProvider`, and `PermissionRegistry` in the `permission` module.

**Why**: The `permission` module has no web or persistence dependencies and is transitively available to any module in the project. This lets any library (e.g., a future `billing-persistence` module) implement `PermissionProvider` without pulling in `permission-web`.

**Alternatives considered**:
- Put models in a new `permission-discovery` module: Adds unnecessary module complexity for a handful of small classes.
- Put models in `permission-web`: Would force any library wanting to declare permissions to depend on `permission-web`, which brings in JAX-RS dependencies.

### CDI `Instance<PermissionProvider>` for aggregation

**Decision**: `PermissionRegistry` injects all `PermissionProvider` beans using `jakarta.enterprise.inject.Instance<PermissionProvider>`.

**Why**: Standard CDI mechanism. Works with Quarkus build-time bean discovery (Jandex indexes are already generated for all subprojects in the root `build.gradle.kts`). Each library jar that ships a `PermissionProvider` bean is automatically included when the downstream app starts.

**Alternatives considered**:
- Classpath resource scanning (`META-INF/revet-permissions.json`): Would work for non-CDI contexts but adds JSON file management overhead and doesn't leverage the existing CDI infrastructure.
- Java `ServiceLoader`: Less integrated with Quarkus CDI; would require separate `META-INF/services` files.

### Flat endpoint at `/.well-known/revet-permissions`

**Decision**: Use IANA's well-known URI path prefix with a `revet-permissions` suffix. Return a flat JSON response grouped by service.

**Why**: The `/.well-known/` path is the standard location for metadata discovery endpoints (RFC 8615). Grouping by service makes it easy for consumers to filter permissions relevant to a specific service.

## Data Flow

```
App startup
├── CDI discovers all PermissionProvider beans (from app + library jars)
├── PermissionRegistry receives Instance<PermissionProvider>
│
GET /.well-known/revet-permissions
├── WellKnownPermissionsResource calls PermissionRegistry.allManifests()
├── Each PermissionProvider.manifest() returns its PermissionManifest
└── Response serializes all manifests as JSON
```

## Example Response

```json
{
  "manifests": [
    {
      "service": "iam",
      "permissions": [
        {
          "action": "iam:CreateUser",
          "description": "Create a new user",
          "resourceType": "urn:revet:iam:{tenantId}:user/{userId}"
        },
        {
          "action": "iam:GetUser",
          "description": "Retrieve a user by ID",
          "resourceType": "urn:revet:iam:{tenantId}:user/{userId}"
        }
      ]
    },
    {
      "service": "billing",
      "permissions": [
        {
          "action": "billing:CreateInvoice",
          "description": "Create an invoice",
          "resourceType": "urn:revet:billing:{tenantId}:invoice/{invoiceId}"
        }
      ]
    }
  ]
}
```

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Developers forget to create a `PermissionProvider` for new permissions | Future annotation scanning build-step can auto-generate providers from `@RequiresPermission` |
| Large number of providers slows endpoint | Permissions are declared in code and are small; aggregation cost is negligible |
| Endpoint exposes internal permission structure | The endpoint is informational metadata; actions/resources are not secrets. If needed, a filter can restrict access. |

## Open Questions

1. Should the endpoint be cached (e.g., computed once at startup and served from memory)?
2. Should the response include a version or hash for change detection by consumers?

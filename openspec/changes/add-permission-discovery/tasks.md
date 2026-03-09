# Tasks: Add Permission Discovery Endpoint

## 1. Domain Models (permission module)
- [x] 1.1 Create `PermissionDeclaration` data class (action, description, resourceType)
- [x] 1.2 Create `PermissionManifest` data class (service, permissions list)
- [x] 1.3 Create `PermissionProvider` interface with `manifest()` method
- [x] 1.4 Create `PermissionRegistry` as `@ApplicationScoped` bean that aggregates all `PermissionProvider` instances

### 1.T Tests for Domain Models
Tests in `permission/src/test/kotlin/.../discovery/`

- [x] 1.T1 PermissionRegistry tests (`PermissionRegistryTest.kt`)
  - Aggregates manifests from multiple providers
  - Returns empty list when no providers exist
  - allPermissions() flattens declarations from all manifests

## 2. REST Endpoint (permission-web module)
- [x] 2.1 Create `WellKnownPermissionsResponse` and `ServiceManifestDto` DTOs
- [x] 2.2 Create `WellKnownPermissionsResource` at `/.well-known/revet-permissions`
- [x] 2.3 Implement GET endpoint that returns aggregated permissions from `PermissionRegistry`

### 2.T Tests for REST Endpoint
Tests in `permission-web/src/test/kotlin/.../api/`

- [x] 2.T1 WellKnownPermissionsResource tests (`WellKnownPermissionsResourceTest.kt`) - use MockK to mock PermissionRegistry
  - Returns aggregated permissions from multiple providers
  - Returns empty manifests when no providers registered
  - Response structure matches expected JSON format

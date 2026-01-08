# Change: Add SCIM v2 API for Users and Groups

## Why

Enable identity provisioning integration with enterprise identity providers (primarily Okta) using the industry-standard SCIM 2.0 protocol. This allows organizations to automatically provision, update, and deprovision users and groups from their IdP.

## What Changes

- **New domain objects**: Add `Group` and `GroupMember` to the `user` module
- **New SCIM API**: JAX-RS resources in `scim/api/` package
  - `UserResource` - CRUD operations for SCIM Users
  - `GroupResource` - CRUD operations for SCIM Groups
  - `ServiceProviderConfigResource` - SCIM discovery endpoint
  - `SchemaResource` - SCIM schema discovery
  - `ResourceTypeResource` - SCIM resource type discovery
- **New DTOs**: SCIM-compliant DTOs in `scim/dtos/` package
- **New mappers**: Domain-to-DTO mappers in `scim/mappers/` package
- **Authentication**: Bearer token authentication
- **OpenAPI**: Eclipse MicroProfile OpenAPI annotations on all endpoints

## Impact

- Affected specs: New `scim-api` capability
- Affected code:
  - `user/` module - new `Group` and `GroupMember` domain classes
  - `scim/` module - new `api/`, `dtos/`, `mappers/` packages

## Endpoints

### User Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/scim/v2/Users` | Create user |
| `GET` | `/scim/v2/Users` | List/search users (pagination + filtering) |
| `GET` | `/scim/v2/Users/{id}` | Get user by ID |
| `PUT` | `/scim/v2/Users/{id}` | Replace user |
| `PATCH` | `/scim/v2/Users/{id}` | Partial update user |
| `DELETE` | `/scim/v2/Users/{id}` | Delete user |

### Group Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/scim/v2/Groups` | Create group |
| `GET` | `/scim/v2/Groups` | List/search groups (pagination + filtering) |
| `GET` | `/scim/v2/Groups/{id}` | Get group by ID |
| `PUT` | `/scim/v2/Groups/{id}` | Replace group |
| `PATCH` | `/scim/v2/Groups/{id}` | Partial update group |
| `DELETE` | `/scim/v2/Groups/{id}` | Delete group |

### Discovery Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/scim/v2/ServiceProviderConfig` | Server capabilities |
| `GET` | `/scim/v2/Schemas` | Available schemas |
| `GET` | `/scim/v2/ResourceTypes` | Resource type definitions |

## Key Design Decisions

- **Users-only group membership**: `GroupMember.type` defaults to `"User"` for future nested group support
- **SCIM filtering**: Support `eq` operator (required by Okta), extensible to other operators
- **Pagination**: `startIndex`/`count` style per SCIM spec, no max page size initially

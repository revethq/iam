# Change: Add Service Accounts

## Why

The IAM system currently only supports person-entities (Users). Non-person entities such as background services, CI/CD pipelines, and automated tools need their own identity to be subject to policy-based access control. These entities authenticate via OAuth 2.1 Client Credentials flow (implemented in the separate auth server) and require IAM-level identity, profiles, and policy attachment.

## What Changes

- **New `service-account` module**: Pure domain model with `ServiceAccount` data class
- **New `service-account-persistence` module**: Quarkus extension with entity, repository, and service (runtime + deployment)
- **New `service-account-web` module**: REST API at `/service-accounts` with CRUD, profile management, and policy listing
- **Modified `ProfileType` enum**: Add `ServiceAccount` value to existing enum in `user` module
- **New URN principal type**: `urn:revet:iam:{tenantId}:service-account/{id}` for policy attachment, constructed via `ServiceAccount.toUrn()`

## Impact

- Affected specs: New `service-accounts` capability
- Affected code:
  - `service-account/` - new module
  - `service-account-persistence/` - new module (runtime + deployment)
  - `service-account-web/` - new module
  - `user/src/.../ProfileType.kt` - add `ServiceAccount` enum value
  - `settings.gradle.kts` - include new modules
  - `build.gradle.kts` (root) - add parent-wrapper skip guard and JReleaser staging repositories

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   service-account-web                        │
├─────────────────────────────────────────────────────────────┤
│  ServiceAccountResource ──────► ServiceAccountService       │
│  ServiceAccountProfileResource ──► ProfileRepository        │
│  ServiceAccountPolicyResource ─► PolicyAttachmentService    │
└──────────────────────────────┼──────────────────────────────┘
                               │ depends on
┌──────────────────────────────▼──────────────────────────────┐
│               service-account-persistence                    │
├─────────────────────────────────────────────────────────────┤
│  ServiceAccountServiceImpl ──► ServiceAccountRepository     │
│                              ─► ServiceAccountEntity        │
└──────────────────────────────┼──────────────────────────────┘
                               │ depends on
┌──────────────────────────────▼──────────────────────────────┐
│                   service-account (domain)                    │
├─────────────────────────────────────────────────────────────┤
│  ServiceAccount (data class)                                 │
└─────────────────────────────────────────────────────────────┘
```

## Key Design Decisions

- **Own module**: Separate module structure follows the existing `user`/`user-persistence`/`user-web` pattern for clean separation
- **No SCIM support**: ServiceAccounts are managed via direct REST API; SCIM is for person-entity provisioning from external IdPs
- **No group membership**: ServiceAccounts get directly attached policies only; group membership can be added later if needed
- **URN-based policy attachment**: Reuses existing `PolicyAttachment` mechanism with `urn:revet:iam:{tenantId}:service-account/{id}` principal URN, constructed via `ServiceAccount.toUrn()`
- **Profile via existing system**: Reuses existing `Profile`/`ProfileRepository` with a new `ServiceAccount` value in `ProfileType` enum

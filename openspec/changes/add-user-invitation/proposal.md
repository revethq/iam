# Change: Add User Invitation System

## Why

The IAM system needs a flexible mechanism for creating users. While direct API-based user creation serves programmatic use cases, many applications require invitation-based onboarding where users validate their identity through email verification or magic links before gaining access. This capability enables both self-service and admin-initiated user provisioning.

## What Changes

- **NEW** `invitation` module with domain models for invitations
- **NEW** `invitation-persistence` module (Quarkus extension) for invitation storage
- **NEW** `Invitation` domain model supporting three creation modes:
  - `DIRECT` - Immediate user creation via API (no validation required)
  - `INVITE` - Email-based verification with validation code
  - `MAGIC_LINK` - Single-use link that auto-validates on click
- **NEW** Configurable completion behavior: auto-create user or require registration flow
- **NEW** Pluggable code generators with Jakarta CDI injection (default: 8-char alphanumeric)
- **NEW** Pluggable message delivery adapters (default: stdout StreamWriter)
- **NEW** Full constraint support: time expiration, usage attempt limits, revocation
- **NEW** Support for both global and application-scoped invitations

## Impact

- Affected specs: `user-invitation` (new capability)
- Affected code:
  - New `invitation/` module (domain models)
  - New `invitation-persistence/` module (Quarkus extension with runtime + deployment)
  - Root `build.gradle.kts` and `settings.gradle.kts` updates
- Dependencies: Uses existing `user` module domain types, `revet-core` metadata patterns

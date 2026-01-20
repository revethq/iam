# Change: Add IAM-Based Permission System

## Why

The platform needs a flexible, fine-grained authorization system that can control access to any resource across any service. AWS IAM's policy model is proven at scale and provides the right balance between expressiveness and simplicity. By using globally unique resource identifiers (similar to ARNs), the permission system becomes use-case agnostic and can authorize access to any resource type.

## What Changes

- **NEW**: Policy domain model with support for Allow/Deny statements
- **NEW**: Principal identifiers using URN format (e.g., `urn:revet:iam::user/alice`)
- **NEW**: Resource identifiers using URN format (e.g., `urn:{namespace}:{service}:{tenant}:{resource-type}/{resource-id}`)
- **NEW**: Action definitions with wildcard support (e.g., `iam:CreateUser`, `s3:*`)
- **NEW**: Condition operators (StringEquals, StringLike, DateLessThan, IpAddress, etc.)
- **NEW**: Policy evaluation engine with deny-by-default semantics
- **NEW**: Managed policy attachment to users and groups (identity-based policies)

## Impact

- Affected specs: Creates new `iam-policies` capability
- Affected code:
  - `permission/` module - Domain models and policy evaluation engine
  - `permission-persistence/` module (new) - Policy storage and retrieval, follows Quarkus extension pattern
  - `user/` module - Principal URN generation
- Dependencies: None (self-contained)

## Design Decisions

1. **URN Format**: Use `urn:{namespace}:{service}:{tenant}:{resource-type}/{resource-id}` for global uniqueness with flexible namespace
2. **Deny-by-Default**: No access unless explicitly allowed (matches AWS IAM behavior)
3. **Explicit Deny Wins**: A Deny statement always overrides Allow statements
4. **Real-time Evaluation**: No caching of authorization decisions; policies evaluated fresh each request
5. **Managed Policies Only**: Inline policies deferred to keep initial implementation simple
6. **Tags Deferred**: Tag-based ABAC will be added when platform-wide tagging is introduced

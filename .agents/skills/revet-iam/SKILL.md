---
name: revet-iam
description: Integration guide for Revet IAM library - identity, access management, and permission evaluation
---

# Revet IAM Library

Identity and access management for Kotlin/Quarkus applications. Provides permission evaluation, user/group management, service accounts, and SCIM 2.0 provisioning.

## Dependency Coordinates

**Group ID:** `com.revethq.iam`
**Version:** `0.1.14`

### Modules

| Artifact | Purpose |
|----------|---------|
| `revet-permission` | Policy/Statement model, URN parsing, policy evaluation, PolicyAttachmentService interface |
| `revet-permission-web` | JAX-RS REST API for policy management |
| `revet-permission-persistence-runtime` | Hibernate Panache persistence for policies |
| `revet-user` | User/Group/Profile domain models |
| `revet-user-web` | JAX-RS REST API for user/group management |
| `revet-user-persistence-runtime` | Hibernate Panache persistence for users/groups |
| `revet-service-account` | ServiceAccount domain model |
| `revet-service-account-persistence` | Hibernate Panache persistence for service accounts |
| `revet-service-account-web` | JAX-RS REST API for service account management |
| `revet-scim` | SCIM 2.0 User/Group provisioning endpoints |

### Gradle

```kotlin
implementation("com.revethq.iam:revet-permission:0.1.14")
implementation("com.revethq.iam:revet-user:0.1.14")
implementation("com.revethq.iam:revet-service-account:0.1.14")
implementation("com.revethq.iam:revet-scim:0.1.14")
```

### Maven

```xml
<dependency>
    <groupId>com.revethq.iam</groupId>
    <artifactId>revet-permission</artifactId>
    <version>0.1.14</version>
</dependency>
```

## Core Concepts

### URN Format

Resources are identified by URNs:
```
urn:{namespace}:{service}:{tenant}:{resourceType}/{resourceId}
```

Examples:
- `urn:revet:iam:acme-corp:user/alice`
- `urn:revet:iam:acme-corp:service-account/550e8400-e29b-41d4-a716-446655440000`

Components:
- `namespace` - Organization namespace (e.g., `revet`)
- `service` - Service identifier (e.g., `iam`, `documents`)
- `tenant` - Tenant/organization identifier (empty for global)
- `resourceType` - Resource category (e.g., `user`, `group`, `policy`)
- `resourceId` - Unique resource identifier

### Policy Model

Policies contain statements that grant or deny permissions:

```kotlin
val policy = Policy(
    id = UUID.randomUUID(),
    name = "user-management-policy",
    version = "2026-01-15",
    statements = listOf(
        Statement(
            effect = Effect.ALLOW,
            actions = listOf("iam:CreateUser", "iam:UpdateUser"),
            resources = listOf("urn:revet:iam:acme-corp:user/*")
        )
    ),
    tenantId = "acme-corp"
)
```

### Authorization Decision Rules

1. **Explicit DENY** - Any matching Deny statement → DENY (highest precedence)
2. **Allow** - Any matching Allow statement (no Deny) → ALLOW
3. **Implicit DENY** - No statements match → DENY (default)

## Related Documentation

- [permissions.md](./permissions.md) - URN format, Policy/Statement classes, condition evaluation
- [users.md](./users.md) - User/Group/Profile data classes, service interfaces
- [service-accounts.md](./service-accounts.md) - ServiceAccount domain, persistence, REST API
- [scim.md](./scim.md) - SCIM 2.0 DTOs, endpoint contracts, filter grammar

## Extension Points

### PolicyCollector

Implement to customize policy retrieval:

```kotlin
@ApplicationScoped
class CustomPolicyCollector : PolicyCollector {
    override fun collectPolicies(principalUrn: String): List<Policy> {
        // Fetch from external IAM, apply caching, filter by tenant
    }
}
```

### PolicyEvaluator

Implement for custom authorization logic:

```kotlin
@ApplicationScoped
class CustomPolicyEvaluator : PolicyEvaluator {
    override fun evaluate(request: AuthorizationRequest): AuthorizationResult {
        // Custom ABAC, audit logging, external service integration
    }
}
```

## Key Constraints

- Policies must have at least one statement
- Statements must have at least one action and one resource
- Policy names are unique per tenant
- `tenantId == null` indicates global policies
- Wildcard `*` matches single path segment; `**` matches hierarchical paths
- Action format: `{service}:{action}` (e.g., `iam:CreateUser`)

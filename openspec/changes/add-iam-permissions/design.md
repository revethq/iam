# Design: IAM-Based Permission System

## Context

The platform requires a flexible authorization system that can:
- Control access to any resource type across any service
- Support fine-grained permissions (action-level, resource-level)
- Provide real-time policy evaluation with consistent deny-by-default semantics

AWS IAM's policy model is the industry standard for cloud authorization and provides a well-understood, battle-tested approach.

## Goals / Non-Goals

### Goals
- Implement core IAM policy concepts (policies, statements, principals, actions, resources, conditions)
- Support managed identity-based policies attached to users and groups
- Provide a policy evaluation engine with correct deny semantics
- Use globally unique identifiers for principals and resources

### Non-Goals
- IAM Roles and assume-role functionality (future enhancement)
- Resource-based policies (policies attached to resources themselves)
- Inline policies (embedded in user/group records)
- Permission boundaries
- Session policies
- Cross-account access
- Policy simulation/validation APIs
- Tag-based ABAC (deferred until platform-wide tagging is introduced)

## Core Concepts

### URN Format (Uniform Resource Name)

All principals and resources use URN format for global uniqueness:

```
urn:{namespace}:{service}:{tenant}:{resource-type}/{resource-id}
```

**Components**:
- `urn:` - Fixed URN prefix
- `{namespace}` - Vendor/platform namespace (e.g., `revet`, `acme`)
- `{service}` - Service namespace (e.g., `iam`, `storage`, `compute`)
- `{tenant}` - Tenant/organization identifier (empty for global resources)
- `{resource-type}` - Type of resource (e.g., `user`, `group`, `bucket`)
- `{resource-id}` - Unique identifier within the resource type

**Examples**:
- `urn:revet:iam::user/alice` - User "alice" (global, revet namespace)
- `urn:revet:iam:acme-corp:group/admins` - Group "admins" in tenant "acme-corp"
- `urn:revet:storage:acme-corp:bucket/reports` - Storage bucket "reports"
- `urn:acme:compute:prod:instance/i-12345` - Compute instance (acme namespace)

**Wildcards**:
- `*` matches any single path segment
- `**` matches zero or more path segments (for hierarchical resources)

**Parsing**:
URNs are parsed using regex: `^urn:([^:]+):([^:]+):([^:]*):([^/]+)/(.+)$`

### Policy Parsing and Validation

Policy documents are validated and parsed in two stages:

1. **JSON Schema validation** (API layer): Validates structure using `org.leadpony.justify` library, consistent with the auth project's schema validation pattern. The policy JSON schema is stored in the database as `Map<String, Any>` with `@JdbcTypeCode(SqlTypes.JSON)`.

2. **Domain object mapping** (Domain layer): Jackson deserializes valid JSON into domain objects (Policy, Statement, Condition). Domain objects handle all business logic: action matching, resource matching, condition evaluation, and policy evaluation.

### Policy Structure

```json
{
  "version": "2026-01-15",
  "statements": [
    {
      "sid": "AllowReadUsers",
      "effect": "Allow",
      "actions": ["iam:GetUser", "iam:ListUsers"],
      "resources": ["urn:revet:iam:acme-corp:user/*"],
      "conditions": {
        "IpAddress": {
          "revet:SourceIp": ["10.0.0.0/8", "192.168.0.0/16"]
        }
      }
    }
  ]
}
```

### Statement Components

| Component | Required | Description |
|-----------|----------|-------------|
| `sid` | No | Statement identifier for debugging/logging |
| `effect` | Yes | `Allow` or `Deny` |
| `actions` | Yes | List of actions (wildcards supported) |
| `resources` | Yes | List of resource URNs (wildcards supported) |
| `conditions` | No | Conditions that must be true for statement to apply |

### Condition Operators

**String Conditions**:
- `StringEquals` / `StringNotEquals` - Exact match (case-sensitive)
- `StringEqualsIgnoreCase` / `StringNotEqualsIgnoreCase` - Case-insensitive
- `StringLike` / `StringNotLike` - Wildcard matching (`*` and `?`)

**Numeric Conditions**:
- `NumericEquals` / `NumericNotEquals`
- `NumericLessThan` / `NumericLessThanEquals`
- `NumericGreaterThan` / `NumericGreaterThanEquals`

**Date Conditions**:
- `DateEquals` / `DateNotEquals`
- `DateLessThan` / `DateLessThanEquals`
- `DateGreaterThan` / `DateGreaterThanEquals`

**Boolean Conditions**:
- `Bool` - Boolean comparison

**IP Address Conditions**:
- `IpAddress` / `NotIpAddress` - CIDR matching

**Existence Conditions**:
- `Null` - Check if key exists

### Condition Context Variables

Variables available in condition values:

| Variable | Description |
|----------|-------------|
| `${revet:CurrentTime}` | Current UTC timestamp |
| `${revet:SourceIp}` | Request source IP |
| `${revet:PrincipalId}` | URN of the calling principal |
| `${revet:RequestedAction}` | Action being requested |
| `${revet:RequestedResource}` | Resource URN being accessed |

Note: Tag-based variables (`${revet:PrincipalTag/key}`, `${revet:ResourceTag/key}`) will be added when platform-wide tagging is introduced.

## Policy Evaluation Algorithm

```
1. Collect all policies attached to the principal (user policies + group policies)
2. Gather all statements from all policies
3. Evaluate each statement:
   a. Check if action matches requested action
   b. Check if resource matches requested resource
   c. Evaluate all conditions (all must be true)
   d. If statement applies, record its effect
4. Determine final decision:
   a. If any Deny statement matched → DENY
   b. If any Allow statement matched → ALLOW
   c. Otherwise → DENY (implicit deny)
```

Key invariants:
- **Explicit deny always wins** over any Allow
- **Implicit deny** when no statements match
- **All conditions must match** for a statement to apply (AND logic)
- **Multiple values in a condition key** use OR logic

## Data Model

### Policy Entity
```
Policy
├── id: UUID
├── name: String (unique per tenant)
├── description: String?
├── version: String ("2026-01-15")
├── statements: List<Statement>
├── tenantId: String? (null for global policies)
├── createdOn: OffsetDateTime
├── updatedOn: OffsetDateTime
└── metadata: Metadata (com.revethq.core.Metadata)
```

### Statement Value Object
```
Statement
├── sid: String?
├── effect: Effect (ALLOW | DENY)
├── actions: List<String>
├── resources: List<String>
└── conditions: Map<String, Map<String, List<String>>>
```

### Policy Attachment
```
PolicyAttachment
├── id: UUID
├── policyId: UUID
├── principalUrn: String
├── attachedOn: OffsetDateTime
└── attachedBy: String?
```

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Complex condition evaluation may be slow | Keep condition keys limited; consider indexing hot paths |
| Policy sprawl makes debugging hard | Provide policy simulation API (future) |
| Large number of group memberships | Limit group-based policies per user; batch policy collection |
| Real-time evaluation adds latency | Keep policies in memory; optimize pattern matching |

## Migration Plan

1. Add policy domain models to `permission/` module
2. Create `permission-persistence/` module following Quarkus extension pattern (deployment + runtime)
3. Add persistence layer with database migrations in `permission-persistence/`
4. Implement policy evaluation engine in `permission/`
5. Add policy management API endpoints
6. Integrate evaluation into authorization middleware

No backward compatibility concerns as this is a new capability.

## Open Questions

1. What is the maximum number of statements per policy?
2. What is the maximum policy size in bytes?
3. Should we support policy versioning (multiple versions of the same policy)?

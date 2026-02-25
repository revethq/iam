## Context

The IAM system needs to support non-person entities (service accounts) that represent background services, CI/CD tools, and automated systems. These entities authenticate via OAuth 2.1 Client Credentials flow (implemented in the separate auth server) and need IAM-level identity for policy-based access control.

**Stakeholders**: Developers building IAM applications with Revet

**Constraints**:
- Follow existing module patterns (domain -> persistence -> web)
- Reuse existing policy attachment and profile mechanisms
- Services work with domain objects, not DTOs
- Packaged as Quarkus extensions for native build support

## Goals / Non-Goals

**Goals**:
- `ServiceAccount` domain model with name and description
- Persistence layer following established Entity/Repository/Service patterns
- REST API for CRUD operations at `/service-accounts`
- Profile management via existing Profile system
- Policy listing via existing PolicyAttachment URN mechanism

**Non-Goals**:
- OAuth Client Credentials flow (handled by auth server)
- Token/credential management (handled by auth server)
- Group membership for service accounts
- SCIM provisioning of service accounts

## Decisions

### 1. Module Structure

Three new modules:

```
service-account/                          # Domain model
service-account-persistence/              # Entity, Repository, Service (flat module with Jandex)
service-account-web/                      # REST API
```

**Rationale**: Unlike `user-persistence` and `permission-persistence`, `service-account-persistence` is a flat module (not a Quarkus extension with runtime/deployment submodules). This avoids the Quarkus extension Gradle plugin's global capability-based dependency substitution, which causes conflicts when multiple extension runtimes coexist in the same dependency graph. Jandex indexing is used for CDI/JPA discovery, which is all that's needed.

### 2. ServiceAccount Domain Model

```kotlin
data class ServiceAccount(
    var id: UUID,
    var name: String,
    var description: String? = null,
    var tenantId: String? = null,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null
) {
    fun toUrn(): String =
        "urn:revet:iam:${tenantId.orEmpty()}:service-account/$id"
}
```

**Rationale**: Minimal fields. `name` is display-only (not unique, not used for lookup). `tenantId` is optional and scopes the service account for multi-tenant deployments; it is also used by `toUrn()` to construct the principal URN for policy attachment. OAuth concerns (`clientId`, credentials) belong to the auth server.

### 3. Persistence Entity

```kotlin
@Entity
@Table(name = "revet_service_accounts")
class ServiceAccountEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var name: String

    @Column(name = "tenant_id")
    var tenantId: String? = null

    var description: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime
}
```

Table: `revet_service_accounts`. Follows `UserEntity` patterns with `toDomain()` and `fromDomain()`.

### 4. Profile Integration

Reuse existing `Profile` and `ProfileRepository` from `user-persistence`. Add `ServiceAccount` to the `ProfileType` enum.

```kotlin
enum class ProfileType {
    User,
    Application,
    ServiceAccount
}
```

Profile endpoints are exposed in `service-account-web`:
- `GET /service-accounts/{id}/profile`
- `PUT /service-accounts/{id}/profile`

**Rationale**: Profiles are already a generic concept with a `resource` UUID and `profileType`. Adding a new enum value is the simplest integration path.

### 5. Policy Integration

ServiceAccount policies use the existing `PolicyAttachment` URN mechanism:
- URN format: `urn:revet:iam:{tenantId}:service-account/{id}` (constructed via `ServiceAccount.toUrn()`)
- Policy listing endpoint: `GET /service-accounts/{id}/policies`

No changes needed to `PolicyAttachment`, `PolicyEvaluator`, or `PolicyCollector` — they already work with arbitrary URN strings. The `toUrn()` method on the domain ensures consistent URN construction.

### 6. REST API Design

```
POST   /service-accounts               -> Create service account
GET    /service-accounts               -> List with pagination
GET    /service-accounts/{id}          -> Get by ID
PUT    /service-accounts/{id}          -> Update
DELETE /service-accounts/{id}          -> Delete
GET    /service-accounts/{id}/profile  -> Get profile
PUT    /service-accounts/{id}/profile  -> Set/update profile
GET    /service-accounts/{id}/policies -> List attached policies
```

DTOs: `CreateServiceAccountRequest`, `UpdateServiceAccountRequest`, `ServiceAccountResponse`.

**Rationale**: Standard REST CRUD following the existing `UserResource` and `GroupResource` patterns in `user-web`.

### 7. PolicyAttachmentService Interface Location

The `PolicyAttachmentService` interface and `AttachedPolicy` data class were moved from `permission-persistence:runtime` to the `permission` domain module (`com.revethq.iam.permission.service` package). This follows the ports-and-adapters pattern: the domain defines the port (interface), and persistence implements it.

**Rationale**: The Quarkus extension plugin registers global capability-based dependency substitution rules. When `service-account-web` depends on both `user-persistence:runtime` and `permission-persistence:runtime`, the plugin substitutes one for the other. By moving the interface to the `permission` domain module (a regular module, not a Quarkus extension), all consumers can access the interface without triggering the substitution.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `ProfileType` in `user` module creates cross-concern | Acceptable — Profile is a generic identity concept; the coupling is minimal (one enum value) |
| No group membership limits policy inheritance | Intentionally deferred; can be added via follow-up proposal if needed |
| Separate module may seem heavy for a simple entity | Follows established patterns; consistency outweighs brevity |
| Flat persistence module (not Quarkus extension) | Jandex indexing provides equivalent bean/entity discovery; no deployment-time build steps needed for this simple module |
| `PolicyAttachmentService` moved to domain module | Architecturally correct (ports-and-adapters); all existing consumers updated |

## Migration Plan

No migration needed — entirely additive. New database table `revet_service_accounts` is created by Hibernate auto-DDL or consumer-managed migrations.

## Open Questions

None.

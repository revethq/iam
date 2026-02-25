## ADDED Requirements

### Requirement: ServiceAccount Domain Model

The system SHALL provide a `ServiceAccount` domain model representing a non-person entity identity. A ServiceAccount SHALL have a display `name`, an optional `description`, an optional `tenantId` for multi-tenant scoping, a `metadata` property bag, and timestamps. The `name` field is for display purposes only and SHALL NOT be used for lookup. The domain model SHALL provide a `toUrn()` method that constructs the principal URN in the format `urn:revet:iam:{tenantId}:service-account/{id}`.

#### Scenario: Create ServiceAccount with required fields
- **WHEN** a ServiceAccount is created with an id and name
- **THEN** a valid ServiceAccount domain object is returned with default metadata and null timestamps

#### Scenario: ServiceAccount toUrn with tenantId
- **WHEN** `toUrn()` is called on a ServiceAccount with tenantId "acme-corp"
- **THEN** the URN `urn:revet:iam:acme-corp:service-account/{id}` is returned

#### Scenario: ServiceAccount toUrn without tenantId
- **WHEN** `toUrn()` is called on a ServiceAccount with no tenantId
- **THEN** the URN `urn:revet:iam::service-account/{id}` is returned

---

### Requirement: ServiceAccount Persistence

The system SHALL provide persistence for ServiceAccount domain objects using Hibernate/Panache in a `revet_service_accounts` database table.

#### Scenario: ServiceAccountEntity maps to domain
- **WHEN** a ServiceAccountEntity is loaded from the database
- **THEN** it can be converted to a ServiceAccount domain object via `toDomain()`

#### Scenario: ServiceAccount domain maps to entity
- **WHEN** a ServiceAccount domain object needs to be persisted
- **THEN** it can be converted to a ServiceAccountEntity via `ServiceAccountEntity.fromDomain()`

#### Scenario: ServiceAccountRepository provides CRUD
- **WHEN** ServiceAccountRepository is used
- **THEN** it provides `findById`, `persist`, and `delete` methods

---

### Requirement: ServiceAccount Service Interface

The system SHALL provide a `ServiceAccountService` interface for ServiceAccount CRUD operations working with domain objects.

#### Scenario: Create service account
- **WHEN** `serviceAccountService.create(serviceAccount)` is called with a valid ServiceAccount
- **THEN** the service account is persisted and returned with timestamps set

#### Scenario: Find service account by ID
- **WHEN** `serviceAccountService.findById(id)` is called with an existing service account ID
- **THEN** the ServiceAccount domain object is returned

#### Scenario: Find service account by ID not found
- **WHEN** `serviceAccountService.findById(id)` is called with a non-existent ID
- **THEN** null is returned

#### Scenario: List service accounts with pagination
- **WHEN** `serviceAccountService.list(startIndex, count)` is called
- **THEN** a Page containing service accounts and total count is returned

#### Scenario: Update service account
- **WHEN** `serviceAccountService.update(serviceAccount)` is called with a modified ServiceAccount
- **THEN** the service account is updated in the database and returned with updated timestamp

#### Scenario: Delete service account
- **WHEN** `serviceAccountService.delete(id)` is called with an existing service account ID
- **THEN** the service account is removed and true is returned

---

### Requirement: ServiceAccount REST API

The system SHALL provide a REST API at `/service-accounts` for managing service accounts with standard CRUD operations.

#### Scenario: Create service account via POST
- **WHEN** POST `/service-accounts` is called with a valid `CreateServiceAccountRequest` body
- **THEN** the service account is created and returned as `ServiceAccountResponse` with HTTP 201

#### Scenario: List service accounts via GET
- **WHEN** GET `/service-accounts` is called with optional pagination parameters
- **THEN** a paginated list of service accounts is returned

#### Scenario: Get service account by ID via GET
- **WHEN** GET `/service-accounts/{id}` is called with an existing ID
- **THEN** the service account is returned as `ServiceAccountResponse`

#### Scenario: Get service account not found
- **WHEN** GET `/service-accounts/{id}` is called with a non-existent ID
- **THEN** HTTP 404 is returned

#### Scenario: Update service account via PUT
- **WHEN** PUT `/service-accounts/{id}` is called with a valid `UpdateServiceAccountRequest` body
- **THEN** the service account is updated and returned as `ServiceAccountResponse`

#### Scenario: Delete service account via DELETE
- **WHEN** DELETE `/service-accounts/{id}` is called with an existing ID
- **THEN** the service account is deleted and HTTP 204 is returned

---

### Requirement: ServiceAccount Profile Management

The system SHALL support profiles for service accounts using the existing Profile system with a `ServiceAccount` profile type.

#### Scenario: Get service account profile
- **WHEN** GET `/service-accounts/{id}/profile` is called for a service account with a profile
- **THEN** the profile is returned as a JSON object

#### Scenario: Get service account profile not found
- **WHEN** GET `/service-accounts/{id}/profile` is called for a service account without a profile
- **THEN** HTTP 404 is returned

#### Scenario: Set service account profile
- **WHEN** PUT `/service-accounts/{id}/profile` is called with a JSON body
- **THEN** the profile is created or updated for the service account with `ProfileType.ServiceAccount`

---

### Requirement: ServiceAccount Policy Listing

The system SHALL support listing policies attached to a service account via the existing PolicyAttachment URN mechanism. The principal URN SHALL be constructed using `ServiceAccount.toUrn()`, producing the format `urn:revet:iam:{tenantId}:service-account/{id}`.

#### Scenario: List attached policies
- **WHEN** GET `/service-accounts/{id}/policies` is called for a service account with attached policies
- **THEN** the list of attached policies is returned

#### Scenario: List attached policies when none exist
- **WHEN** GET `/service-accounts/{id}/policies` is called for a service account with no attached policies
- **THEN** an empty list is returned

---

### Requirement: ServiceAccount Quarkus Extension

The `service-account-persistence` module SHALL be packaged as a Quarkus extension with `runtime` and `deployment` submodules for native build support.

#### Scenario: Extension registers entities at build time
- **WHEN** the Quarkus application is built with `service-account-persistence` on the classpath
- **THEN** `ServiceAccountEntity` is registered for reflection and JPA processing via the deployment processor

---

### Requirement: ServiceAccount Maven Central Publishing

All three service account modules (`service-account`, `service-account-persistence`, `service-account-web`) SHALL be published to Maven Central via JReleaser, following the existing convention-based publishing setup.

#### Scenario: Modules are included in JReleaser staging
- **WHEN** the JReleaser publish workflow runs
- **THEN** the `service-account`, `service-account-persistence/runtime`, `service-account-persistence/deployment`, and `service-account-web` staging repositories are deployed to Maven Central

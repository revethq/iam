## 1. Domain Model

- [x] 1.1 Create `service-account` Gradle module with `build.gradle.kts`
- [x] 1.2 Create `ServiceAccount` data class in `com.revethq.iam.serviceaccount.domain` with `toUrn()` method

## 2. ProfileType Update

- [x] 2.1 Add `ServiceAccount` value to `ProfileType` enum in `user` module

## 3. Persistence Entities

- [x] 3.1 Create `service-account-persistence` flat Gradle module with `build.gradle.kts` (not a Quarkus extension — uses Jandex for CDI/JPA discovery)
- [x] 3.2 Create `ServiceAccountEntity` with `toDomain()` and `fromDomain()`

> **Note:** The original plan called for a Quarkus extension (runtime/deployment) structure. This was changed to a flat module to avoid the Quarkus extension Gradle plugin's global capability-based dependency substitution, which conflicts when multiple extension runtimes coexist in the same dependency graph.

## 4. Repository

- [x] 4.1 Create `ServiceAccountRepository` extending `PanacheRepositoryBase<ServiceAccountEntity, UUID>`

## 5. Service

- [x] 5.1 Create `ServiceAccountService` interface with CRUD methods
- [x] 5.2 Create `ServiceAccountServiceImpl` implementing `ServiceAccountService`
- [x] 5.3 Implement `create(serviceAccount: ServiceAccount)`
- [x] 5.4 Implement `findById(id: UUID)`
- [x] 5.5 Implement `list(startIndex: Int, count: Int)`
- [x] 5.6 Implement `update(serviceAccount: ServiceAccount)`
- [x] 5.7 Implement `delete(id: UUID)`
- [x] 5.8 Implement `count()`

## 6. Interface Refactoring

- [x] 6.1 Move `PolicyAttachmentService` interface and `AttachedPolicy` data class from `permission-persistence:runtime` to `permission` domain module (`com.revethq.iam.permission.service` package)
- [x] 6.2 Update all imports across `permission-persistence:runtime`, `permission-web`, and `service-account-web`

> **Note:** This was required because the Quarkus extension plugin applies global dependency substitution rules, causing `permission-persistence:runtime` to be substituted when `user-persistence:runtime` is also in the dependency graph. Moving the interface to the `permission` domain module follows the ports-and-adapters pattern.

## 7. REST API

- [x] 7.1 Create `service-account-web` Gradle module with `build.gradle.kts`
- [x] 7.2 Create `CreateServiceAccountRequest` DTO
- [x] 7.3 Create `UpdateServiceAccountRequest` DTO
- [x] 7.4 Create `ServiceAccountResponse` DTO
- [x] 7.5 Create `ServiceAccountResource` at `/service-accounts` with CRUD endpoints
- [x] 7.6 Create `ServiceAccountProfileResource` at `/service-accounts/{id}/profile`
- [x] 7.7 Create `ServiceAccountPolicyResource` at `/service-accounts/{id}/policies`

## 8. Exception Handling

- [x] 8.1 Create `ServiceAccountNotFoundException`
- [x] 8.2 Create `ServiceAccountConflictException`
- [x] 8.3 Create `ExceptionMapper` providers for both exceptions

## 9. Build and Publishing

- [x] 9.1 Add `include` lines for `service-account`, `service-account-persistence`, and `service-account-web` to `settings.gradle.kts`
- [x] 9.2 Add JReleaser `stagingRepository` entries for `service-account`, `service-account-persistence`, and `service-account-web`

## 10. Testing

- [x] 10.1 Write unit tests for `ServiceAccountEntity.toDomain()` and `fromDomain()`
- [x] 10.2 Write unit tests for `ServiceAccountServiceImpl`
- [x] 10.3 Write unit tests for `ServiceAccountResource`
- [x] 10.4 Write unit tests for `ServiceAccountProfileResource`
- [x] 10.5 Write unit tests for `ServiceAccountPolicyResource`

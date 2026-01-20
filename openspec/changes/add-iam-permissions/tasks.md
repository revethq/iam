# Tasks: Add IAM-Based Permission System

## 1. Domain Models (permission module)
- [x] 1.1 Create URN value class with parsing, validation, and matching logic
- [x] 1.2 Create Effect enum (ALLOW, DENY)
- [x] 1.3 Create Statement data class with action/resource matching
- [x] 1.4 Create Policy data class with version and statement list
- [x] 1.5 Create PolicyAttachment data class

### 1.T Tests for Domain Models
Tests in `permission/src/test/kotlin/.../domain/`

- [x] 1.T1 URN parsing tests (`UrnTest.kt`)
  - Parse valid URN with tenant (extracts namespace, service, tenant, resourceType, resourceId)
  - Parse valid URN without tenant
  - Parse URN with custom namespace (e.g., `urn:acme:...`)
  - Parse URN with path segments in resourceId
  - Reject invalid URN format
  - Reject URN with missing components
- [x] 1.T2 URN wildcard matching tests
  - Exact match
  - Single segment wildcard (`*`) matches one segment
  - Single segment wildcard does not match nested paths
  - Multi-segment wildcard (`**`) matches nested paths
  - Wildcard in middle of path
- [x] 1.T3 Action matching tests (`ActionMatcherTest.kt`)
  - Exact action match
  - Service wildcard match (`iam:*`)
  - Full wildcard match (`*`)
  - No match when action differs
- [x] 1.T4 Statement validation tests (`StatementTest.kt`)
  - Valid statement with all required fields
  - Reject statement without actions
  - Reject statement without resources

## 2. Condition Evaluation (permission module)
- [x] 2.1 Create ConditionOperator enum with all supported operators
- [x] 2.2 Create ConditionContext class with context variable resolution
- [x] 2.3 Implement StringEquals/StringNotEquals operators
- [x] 2.4 Implement StringEqualsIgnoreCase/StringNotEqualsIgnoreCase operators
- [x] 2.5 Implement StringLike/StringNotLike with wildcard matching
- [x] 2.6 Implement numeric comparison operators
- [x] 2.7 Implement date comparison operators
- [x] 2.8 Implement Bool operator
- [x] 2.9 Implement IpAddress/NotIpAddress with CIDR matching
- [x] 2.10 Implement Null operator for existence checks
- [x] 2.11 Implement variable substitution (${revet:...} syntax)

### 2.T Tests for Condition Evaluation
Tests in `permission/src/test/kotlin/.../condition/`

- [x] 2.T1 String operator tests (`StringConditionTest.kt`)
  - StringEquals exact match
  - StringEquals case sensitivity
  - StringNotEquals
  - StringEqualsIgnoreCase
  - StringLike with wildcards (* and ?)
  - StringNotLike
- [x] 2.T2 Numeric operator tests (`NumericConditionTest.kt`)
  - NumericEquals / NumericNotEquals
  - NumericLessThan / NumericLessThanEquals
  - NumericGreaterThan / NumericGreaterThanEquals
  - Invalid numeric value handling
- [x] 2.T3 Date operator tests (`DateConditionTest.kt`)
  - DateEquals / DateNotEquals
  - DateLessThan / DateLessThanEquals
  - DateGreaterThan / DateGreaterThanEquals
  - ISO 8601 format parsing
- [x] 2.T4 Other operator tests (`MiscConditionTest.kt`)
  - Bool operator true/false
  - IpAddress CIDR matching (10.0.0.0/8)
  - NotIpAddress
  - Null existence check (key exists vs missing)
- [x] 2.T5 Variable substitution tests (`ConditionContextTest.kt`)
  - Resolve ${revet:PrincipalId}
  - Resolve ${revet:CurrentTime}
  - Resolve ${revet:SourceIp}
  - Resolve ${revet:RequestedAction}
  - Resolve ${revet:RequestedResource}
  - Unknown variable resolves to empty string
- [x] 2.T6 Condition combination tests
  - Multiple conditions AND together
  - Multiple values for same key OR together

## 3. Policy Evaluation Engine (permission module)
- [x] 3.1 Create AuthorizationRequest class (principal, action, resource, context)
- [x] 3.2 Create AuthorizationResult class (decision, matching statements)
- [x] 3.3 Implement statement matching (action + resource + conditions)
- [x] 3.4 Implement policy collector (gather policies from user and groups)
- [x] 3.5 Implement evaluation algorithm (explicit deny > allow > implicit deny)
- [x] 3.6 Create PolicyEvaluator service interface
- [x] 3.7 Implement PolicyEvaluator with real-time evaluation

### 3.T Tests for Policy Evaluation Engine
Tests in `permission/src/test/kotlin/.../evaluation/`

- [x] 3.T1 Policy evaluation tests (`PolicyEvaluatorTest.kt`) - use MockK to mock policy collector
  - Implicit deny when no policies attached
  - Single Allow statement grants access
  - Single Deny statement denies access
  - Explicit deny overrides allow (two policies)
  - Condition failure prevents statement match
  - Action mismatch prevents statement match
  - Resource mismatch prevents statement match
- [x] 3.T2 Policy collector tests (`PolicyCollectorTest.kt`) - use MockK to mock services
  - Collect policies attached to user
  - Collect policies from user's groups
  - Combine user and group policies
  - Handle user with no policies
  - Handle user with no groups

## 4. Persistence Layer (permission-persistence module)
- [x] 4.1 Create `permission-persistence/` module with Quarkus extension structure (deployment + runtime)
- [x] 4.2 Configure build.gradle.kts for the new module
- [x] 4.3 Create PolicyEntity with JPA annotations
- [x] 4.4 Create StatementEntity (embedded or separate table) - Stored as JSONB in PolicyEntity
- [x] 4.5 Create PolicyAttachmentEntity
- [x] 4.6 Create PolicyRepository interface
- [x] 4.7 Create PolicyAttachmentRepository interface
- [x] 4.8 Implement PolicyService for CRUD operations
- [x] 4.9 Implement PolicyAttachmentService
- [x] 4.10 Create database migrations for policy tables

### 4.T Tests for Persistence Layer
Tests in `permission-persistence/runtime/src/test/kotlin/.../service/`

- [x] 4.T1 PolicyService tests (`PolicyServiceImplTest.kt`) - use MockK to mock repository
  - Create persists policy and returns domain object
  - FindById returns policy when found
  - FindById returns null when not found
  - FindByName returns policy when found
  - Update modifies existing policy
  - Delete returns true when policy deleted
  - Delete returns false when policy not found
- [x] 4.T2 PolicyAttachmentService tests (`PolicyAttachmentServiceImplTest.kt`) - use MockK
  - Attach policy to principal
  - Attach same policy twice fails
  - Detach policy from principal
  - List attachments for policy
  - List policies for principal

## 5. Policy Management API (permission-web module)
- [ ] 5.1 Define policy JSON Schema and store in database (following auth project pattern) - Deferred
- [ ] 5.2 Create PolicySchemaValidator using `org.leadpony.justify` (like auth's SchemaService) - Deferred
- [x] 5.3 Create CreatePolicyRequest/Response DTOs
- [x] 5.4 Create UpdatePolicyRequest/Response DTOs
- [x] 5.5 Create PolicyResource REST endpoint
- [x] 5.6 Implement POST /policies (validate with JSON Schema, then deserialize to domain)
- [x] 5.7 Implement GET /policies/{id} (get policy)
- [x] 5.8 Implement GET /policies (list policies with filtering)
- [x] 5.9 Implement PUT /policies/{id} (update policy)
- [x] 5.10 Implement DELETE /policies/{id} (delete policy)
- [x] 5.11 Create AttachPolicyRequest DTO
- [x] 5.12 Implement POST /policies/{id}/attachments (attach to principal)
- [x] 5.13 Implement DELETE /policies/{id}/attachments/{principalUrn} (detach)
- [x] 5.14 Implement GET /policies/{id}/attachments (list attachments)

### 5.T Tests for Policy Management API
Tests in `permission-web/src/test/kotlin/.../api/`

- [ ] 5.T1 PolicySchemaValidator tests (`PolicySchemaValidatorTest.kt`) - Deferred (JSON Schema validation not implemented)
- [x] 5.T2 PolicyResource tests (`PolicyResourceTest.kt`) - use MockK to mock PolicyService
  - createPolicy returns 201 with created policy
  - createPolicy throws conflict when name exists
  - getPolicy returns policy when found
  - getPolicy throws not found when missing
  - listPolicies returns paginated results
  - updatePolicy updates existing policy
  - updatePolicy throws not found when missing
  - deletePolicy returns 204 on success
  - deletePolicy throws not found when missing
- [x] 5.T3 Policy attachment tests (in `PolicyResourceTest.kt`) - use MockK
  - attachPolicy returns 201 on success
  - attachPolicy throws conflict when already attached
  - detachPolicy returns 204 on success
  - listAttachments returns attachments for policy

## 6. Authorization Middleware (permission-web module)
- [x] 6.1 Create AuthorizationContext request-scoped bean
- [x] 6.2 Create @RequiresPermission annotation
- [x] 6.3 Implement authorization interceptor/filter
- [x] 6.4 Integrate PolicyEvaluator into authorization flow

### 6.T Tests for Authorization Middleware
- [x] 6.T1 Authorization interceptor tests (`AuthorizationFilterTest.kt`) - use MockK
  - Allow access when PolicyEvaluator returns ALLOW
  - Deny access when PolicyEvaluator returns DENY
  - Extract principal from security context
  - Build correct AuthorizationRequest from annotation

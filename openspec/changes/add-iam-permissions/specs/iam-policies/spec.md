## ADDED Requirements

### Requirement: Policy Structure
The system SHALL support IAM policies containing one or more statements that define access permissions.

A policy MUST have:
- A unique identifier (UUID)
- A name (unique within a tenant)
- A version string indicating the policy language version
- One or more statements

A policy MAY have:
- A description
- A tenant identifier (null for global policies)
- Metadata key-value pairs

#### Scenario: Create a valid policy
- **WHEN** a policy is created with name "ReadOnlyUsers", version "2026-01-15", and one statement
- **THEN** the policy is persisted with a generated UUID
- **AND** createdOn and updatedOn timestamps are set

#### Scenario: Policy name uniqueness within tenant
- **WHEN** a policy with name "AdminPolicy" exists in tenant "acme-corp"
- **AND** another policy with name "AdminPolicy" is created in tenant "acme-corp"
- **THEN** the creation fails with a duplicate name error

#### Scenario: Same policy name in different tenants
- **WHEN** a policy with name "AdminPolicy" exists in tenant "acme-corp"
- **AND** a policy with name "AdminPolicy" is created in tenant "other-corp"
- **THEN** the creation succeeds

---

### Requirement: Statement Structure
Each statement within a policy SHALL define an effect, actions, and resources.

A statement MUST have:
- An effect: either "Allow" or "Deny"
- One or more actions
- One or more resources

A statement MAY have:
- A statement identifier (sid) for debugging
- Conditions that must evaluate to true for the statement to apply

#### Scenario: Valid Allow statement
- **WHEN** a statement has effect "Allow", actions ["iam:GetUser"], and resources ["urn:revet:iam::user/*"]
- **THEN** the statement is valid

#### Scenario: Statement without actions is invalid
- **WHEN** a statement has effect "Allow" and resources but no actions
- **THEN** validation fails with "actions required" error

#### Scenario: Statement without resources is invalid
- **WHEN** a statement has effect "Allow" and actions but no resources
- **THEN** validation fails with "resources required" error

---

### Requirement: URN Format
Principals and resources SHALL be identified using Uniform Resource Names (URNs) in the format `urn:{namespace}:{service}:{tenant}:{resource-type}/{resource-id}`.

The URN format MUST support:
- Namespace identifier (required, e.g., "revet", "acme")
- Service namespace (required)
- Tenant identifier (optional, empty string for global resources)
- Resource type (required)
- Resource identifier with path segments (required)

#### Scenario: Parse valid URN with tenant
- **WHEN** the URN "urn:revet:storage:acme-corp:bucket/my-bucket" is parsed
- **THEN** namespace is "revet"
- **AND** service is "storage"
- **AND** tenant is "acme-corp"
- **AND** resource type is "bucket"
- **AND** resource id is "my-bucket"

#### Scenario: Parse valid URN without tenant
- **WHEN** the URN "urn:revet:iam::user/alice" is parsed
- **THEN** namespace is "revet"
- **AND** service is "iam"
- **AND** tenant is empty
- **AND** resource type is "user"
- **AND** resource id is "alice"

#### Scenario: Parse URN with custom namespace
- **WHEN** the URN "urn:acme:compute:prod:instance/i-12345" is parsed
- **THEN** namespace is "acme"
- **AND** service is "compute"
- **AND** tenant is "prod"
- **AND** resource type is "instance"
- **AND** resource id is "i-12345"

#### Scenario: Parse URN with path segments
- **WHEN** the URN "urn:revet:storage:acme:object/bucket/folder/file.txt" is parsed
- **THEN** resource type is "object"
- **AND** resource id is "bucket/folder/file.txt"

#### Scenario: Invalid URN format rejected
- **WHEN** the URN "invalid:format" is parsed
- **THEN** parsing fails with "invalid URN format" error

---

### Requirement: Action Matching
The policy evaluation engine SHALL match requested actions against statement actions, supporting wildcard patterns.

Action patterns MUST support:
- Exact match (e.g., "iam:CreateUser")
- Service wildcard (e.g., "iam:*" matches all iam actions)
- Full wildcard (e.g., "*" matches all actions)

#### Scenario: Exact action match
- **WHEN** statement actions contain "iam:CreateUser"
- **AND** requested action is "iam:CreateUser"
- **THEN** the action matches

#### Scenario: Service wildcard match
- **WHEN** statement actions contain "iam:*"
- **AND** requested action is "iam:DeleteUser"
- **THEN** the action matches

#### Scenario: Full wildcard match
- **WHEN** statement actions contain "*"
- **AND** requested action is "storage:PutObject"
- **THEN** the action matches

#### Scenario: No match when action differs
- **WHEN** statement actions contain "iam:CreateUser"
- **AND** requested action is "iam:DeleteUser"
- **THEN** the action does not match

---

### Requirement: Resource Matching
The policy evaluation engine SHALL match requested resources against statement resources, supporting wildcard patterns.

Resource patterns MUST support:
- Exact URN match
- Single segment wildcard (`*`) matching any single path segment
- Multi-segment wildcard (`**`) matching zero or more path segments
- Wildcards in resource-id portion only

#### Scenario: Exact resource match
- **WHEN** statement resources contain "urn:revet:iam:acme:user/alice"
- **AND** requested resource is "urn:revet:iam:acme:user/alice"
- **THEN** the resource matches

#### Scenario: Single segment wildcard match
- **WHEN** statement resources contain "urn:revet:iam:acme:user/*"
- **AND** requested resource is "urn:revet:iam:acme:user/alice"
- **THEN** the resource matches

#### Scenario: Single wildcard does not match nested paths
- **WHEN** statement resources contain "urn:revet:storage:acme:object/*"
- **AND** requested resource is "urn:revet:storage:acme:object/folder/file.txt"
- **THEN** the resource does not match

#### Scenario: Multi-segment wildcard matches nested paths
- **WHEN** statement resources contain "urn:revet:storage:acme:object/**"
- **AND** requested resource is "urn:revet:storage:acme:object/folder/subfolder/file.txt"
- **THEN** the resource matches

#### Scenario: Wildcard in middle of path
- **WHEN** statement resources contain "urn:revet:storage:acme:object/*/file.txt"
- **AND** requested resource is "urn:revet:storage:acme:object/folder/file.txt"
- **THEN** the resource matches

---

### Requirement: Condition Evaluation
The policy evaluation engine SHALL evaluate conditions to determine if a statement applies.

Conditions MUST support these operators:
- StringEquals, StringNotEquals (case-sensitive exact match)
- StringEqualsIgnoreCase, StringNotEqualsIgnoreCase (case-insensitive)
- StringLike, StringNotLike (wildcard matching with * and ?)
- NumericEquals, NumericNotEquals, NumericLessThan, NumericLessThanEquals, NumericGreaterThan, NumericGreaterThanEquals
- DateEquals, DateNotEquals, DateLessThan, DateLessThanEquals, DateGreaterThan, DateGreaterThanEquals
- Bool (boolean comparison)
- IpAddress, NotIpAddress (CIDR matching)
- Null (existence check)

All conditions within a statement MUST evaluate to true for the statement to apply (AND logic).
Multiple values for a single condition key MUST use OR logic.

#### Scenario: StringEquals condition
- **WHEN** condition is {"StringEquals": {"revet:RequestedAction": ["iam:GetUser"]}}
- **AND** requested action is "iam:GetUser"
- **THEN** condition evaluates to true

#### Scenario: StringEquals with multiple values (OR logic)
- **WHEN** condition is {"StringEquals": {"revet:RequestedAction": ["iam:GetUser", "iam:ListUsers"]}}
- **AND** requested action is "iam:ListUsers"
- **THEN** condition evaluates to true

#### Scenario: Multiple conditions (AND logic)
- **WHEN** conditions are {"StringLike": {"revet:RequestedResource": ["urn:revet:iam:acme:*"]}, "Bool": {"revet:SecureTransport": ["true"]}}
- **AND** requested resource is "urn:revet:iam:acme:user/alice"
- **AND** request uses secure transport
- **THEN** conditions evaluate to true

#### Scenario: IpAddress CIDR matching
- **WHEN** condition is {"IpAddress": {"revet:SourceIp": ["10.0.0.0/8"]}}
- **AND** request source IP is "10.1.2.3"
- **THEN** condition evaluates to true

#### Scenario: Null existence check
- **WHEN** condition is {"Null": {"revet:SourceIp": ["false"]}}
- **AND** request has a source IP
- **THEN** condition evaluates to true (SourceIp exists, so Null is false)

---

### Requirement: Condition Context Variables
Condition values SHALL support variable substitution using `${variable}` syntax.

The following context variables MUST be available:
- `${revet:CurrentTime}` - Current UTC timestamp (ISO 8601)
- `${revet:SourceIp}` - Request source IP address
- `${revet:PrincipalId}` - URN of the calling principal
- `${revet:RequestedAction}` - Action being requested
- `${revet:RequestedResource}` - Resource URN being accessed

Note: Tag-based variables (`${revet:PrincipalTag/key}`, `${revet:ResourceTag/key}`) will be supported when platform-wide tagging is introduced.

#### Scenario: Variable substitution in condition
- **WHEN** condition value is "${revet:PrincipalId}"
- **AND** principal URN is "urn:revet:iam::user/alice"
- **THEN** the value resolves to "urn:revet:iam::user/alice"

#### Scenario: CurrentTime variable
- **WHEN** condition value is "${revet:CurrentTime}"
- **THEN** the value resolves to the current UTC timestamp in ISO 8601 format

#### Scenario: Unknown variable
- **WHEN** condition value is "${revet:UnknownVariable}"
- **THEN** the value resolves to empty string

---

### Requirement: Policy Evaluation
The policy evaluation engine SHALL evaluate authorization requests against all applicable policies and return an allow or deny decision.

The evaluation MUST follow these rules:
1. Collect all policies attached to the principal (directly and via groups)
2. Evaluate each statement in each policy
3. A statement applies if: action matches AND resource matches AND all conditions are true
4. Final decision:
   - DENY if any matching statement has effect Deny (explicit deny)
   - ALLOW if any matching statement has effect Allow and no Deny matched
   - DENY if no statements match (implicit deny)

#### Scenario: Implicit deny when no policies
- **WHEN** principal has no attached policies
- **AND** an authorization check is performed
- **THEN** the result is DENY

#### Scenario: Single Allow statement grants access
- **WHEN** principal has a policy with statement effect=Allow matching the request
- **AND** no Deny statements match
- **THEN** the result is ALLOW

#### Scenario: Explicit deny overrides allow
- **WHEN** principal has policy A with statement effect=Allow matching the request
- **AND** principal has policy B with statement effect=Deny matching the request
- **THEN** the result is DENY

#### Scenario: Condition failure prevents statement match
- **WHEN** principal has a policy with statement effect=Allow
- **AND** statement action and resource match the request
- **AND** statement conditions do not evaluate to true
- **THEN** the statement does not apply
- **AND** the result is DENY (implicit deny)

#### Scenario: Group policies are evaluated
- **WHEN** principal belongs to group "developers"
- **AND** group "developers" has an attached policy with effect=Allow matching the request
- **THEN** the result is ALLOW

---

### Requirement: Policy Attachment
Policies SHALL be attachable to principals (users and groups) to grant permissions.

The system MUST support:
- Attaching managed policies to users
- Attaching managed policies to groups
- Detaching policies from users and groups
- Listing policies attached to a principal
- Listing principals attached to a policy

#### Scenario: Attach policy to user
- **WHEN** policy "ReadOnlyAccess" is attached to user "alice"
- **THEN** the attachment is recorded with timestamp
- **AND** user "alice" authorization checks include this policy

#### Scenario: Attach policy to group
- **WHEN** policy "DeveloperAccess" is attached to group "developers"
- **THEN** all members of "developers" inherit the policy for authorization

#### Scenario: Detach policy from user
- **WHEN** policy "ReadOnlyAccess" is detached from user "alice"
- **THEN** user "alice" authorization checks no longer include this policy

#### Scenario: Attach same policy twice fails
- **WHEN** policy "ReadOnlyAccess" is already attached to user "alice"
- **AND** an attempt is made to attach it again
- **THEN** the operation fails with "already attached" error


## ADDED Requirements

### Requirement: Group Persistence

The system SHALL provide persistence for Group domain objects using Hibernate/Panache.

#### Scenario: GroupEntity maps to domain
- **WHEN** a GroupEntity is loaded from the database
- **THEN** it can be converted to a Group domain object via `toDomain()`

#### Scenario: Group domain maps to entity
- **WHEN** a Group domain object needs to be persisted
- **THEN** it can be converted to a GroupEntity via `GroupEntity.fromDomain()`

#### Scenario: GroupRepository provides CRUD
- **WHEN** GroupRepository is used
- **THEN** it provides `findById`, `persist`, `delete`, and query methods

---

### Requirement: GroupMember Persistence

The system SHALL provide persistence for GroupMember domain objects using Hibernate/Panache.

#### Scenario: GroupMemberEntity maps to domain
- **WHEN** a GroupMemberEntity is loaded from the database
- **THEN** it can be converted to a GroupMember domain object via `toDomain()`

#### Scenario: GroupMember domain maps to entity
- **WHEN** a GroupMember domain object needs to be persisted
- **THEN** it can be converted to a GroupMemberEntity via `GroupMemberEntity.fromDomain()`

#### Scenario: GroupMemberRepository provides member queries
- **WHEN** GroupMemberRepository is used
- **THEN** it provides methods to find members by groupId and manage memberships

---

### Requirement: User Service Interface

The system SHALL provide a UserService interface for user CRUD operations working with domain objects.

#### Scenario: Create user
- **WHEN** `userService.create(user)` is called with a valid User
- **THEN** the user is persisted and returned with timestamps set

#### Scenario: Find user by ID
- **WHEN** `userService.findById(id)` is called with an existing user ID
- **THEN** the User domain object is returned

#### Scenario: Find user by ID not found
- **WHEN** `userService.findById(id)` is called with a non-existent ID
- **THEN** null is returned

#### Scenario: List users with pagination
- **WHEN** `userService.list(startIndex, count)` is called
- **THEN** a Page containing users and total count is returned

#### Scenario: Update user
- **WHEN** `userService.update(user)` is called with a modified User
- **THEN** the user is updated in the database and returned with updated timestamp

#### Scenario: Delete user
- **WHEN** `userService.delete(id)` is called with an existing user ID
- **THEN** the user is removed and true is returned

---

### Requirement: Group Service Interface

The system SHALL provide a GroupService interface for group CRUD and member management operations.

#### Scenario: Create group
- **WHEN** `groupService.create(group)` is called with a valid Group
- **THEN** the group is persisted and returned with timestamps set

#### Scenario: Find group by ID
- **WHEN** `groupService.findById(id)` is called with an existing group ID
- **THEN** the Group domain object is returned

#### Scenario: List groups with pagination
- **WHEN** `groupService.list(startIndex, count)` is called
- **THEN** a Page containing groups and total count is returned

#### Scenario: Get group members
- **WHEN** `groupService.getMembers(groupId)` is called
- **THEN** a list of GroupMember objects for that group is returned

#### Scenario: Add member to group
- **WHEN** `groupService.addMember(groupId, member)` is called
- **THEN** the member is added to the group and returned

#### Scenario: Remove member from group
- **WHEN** `groupService.removeMember(groupId, memberId)` is called
- **THEN** the member is removed and true is returned

#### Scenario: Set group members
- **WHEN** `groupService.setMembers(groupId, members)` is called
- **THEN** existing members are replaced with the new list

---

### Requirement: Concrete SCIM User Resource

The system SHALL provide a concrete UserResource that delegates to UserService.

#### Scenario: UserResource injects UserService
- **WHEN** UserResource is instantiated by CDI
- **THEN** it receives a UserService instance via constructor injection

#### Scenario: Create user delegates to service
- **WHEN** POST /scim/v2/Users is called
- **THEN** UserResource converts ScimUser to domain, calls userService.create, and returns ScimUser

#### Scenario: List users delegates to service
- **WHEN** GET /scim/v2/Users is called with pagination params
- **THEN** UserResource calls userService.list and converts results to ScimListResponse

#### Scenario: Get user delegates to service
- **WHEN** GET /scim/v2/Users/{id} is called
- **THEN** UserResource calls userService.findById and returns ScimUser or 404

---

### Requirement: Concrete SCIM Group Resource

The system SHALL provide a concrete GroupResource that delegates to GroupService.

#### Scenario: GroupResource injects GroupService
- **WHEN** GroupResource is instantiated by CDI
- **THEN** it receives GroupService and UserService instances via constructor injection

#### Scenario: Create group delegates to service
- **WHEN** POST /scim/v2/Groups is called
- **THEN** GroupResource converts ScimGroup to domain, calls groupService.create, and returns ScimGroup

#### Scenario: Patch group add member
- **WHEN** PATCH /scim/v2/Groups/{id} is called with add operation for members
- **THEN** GroupResource calls groupService.addMember for each member

#### Scenario: Patch group remove member
- **WHEN** PATCH /scim/v2/Groups/{id} is called with remove operation for members
- **THEN** GroupResource calls groupService.removeMember for each member

---

### Requirement: Page Result Type

The system SHALL provide a Page data class for paginated query results.

#### Scenario: Page contains items and metadata
- **WHEN** a Page is created
- **THEN** it contains items list, totalCount, startIndex, and itemsPerPage

#### Scenario: Page maps to ScimListResponse
- **WHEN** a Page of domain objects is returned from a service
- **THEN** it can be converted to a ScimListResponse with correct pagination values

## Context

This design extends the SCIM v2 API implementation by wiring it to the `user-persistence` Quarkus extension. The SCIM resources currently define abstract methods that consumers must implement. This change makes them concrete by introducing service interfaces and implementations backed by Panache repositories.

**Stakeholders**: Developers building IAM applications with Revet

**Constraints**:
- Follow existing `user-persistence` patterns (Entity, Repository, Quarkus extension)
- Services work with domain objects, not DTOs
- Maintain separation: SCIM concerns stay in `scim`, persistence in `user-persistence`

## Goals / Non-Goals

**Goals**:
- Concrete `UserResource` and `GroupResource` ready for use
- Service interfaces that can be mocked for testing
- Group and GroupMember persistence following existing patterns
- Transactional support for SCIM operations

**Non-Goals**:
- Custom query DSL for SCIM filtering (use simple repository methods)
- Bulk operation optimization
- Caching layer

## Decisions

### 1. Service Interface Location

Services live in `user-persistence/runtime`:

```
user-persistence/runtime/src/main/kotlin/com/revethq/iam/user/persistence/
├── entity/
│   ├── UserEntity.kt          (existing)
│   ├── GroupEntity.kt         (new)
│   └── GroupMemberEntity.kt   (new)
├── repository/
│   ├── UserRepository.kt      (existing)
│   ├── GroupRepository.kt     (new)
│   └── GroupMemberRepository.kt (new)
└── service/
    ├── UserService.kt         (new - interface)
    ├── UserServiceImpl.kt     (new - implementation)
    ├── GroupService.kt        (new - interface)
    └── GroupServiceImpl.kt    (new - implementation)
```

**Rationale**: Services are part of the persistence layer, working with domain objects and repositories. The `scim` module depends on `user-persistence` to access these services.

### 2. Service Interface Design

```kotlin
interface UserService {
    fun create(user: User): User
    fun findById(id: UUID): User?
    fun findByUsername(username: String): User?
    fun findByExternalId(externalId: String): User?
    fun list(startIndex: Int, count: Int): Page<User>
    fun update(user: User): User
    fun delete(id: UUID): Boolean
    fun count(): Long
}

interface GroupService {
    fun create(group: Group): Group
    fun findById(id: UUID): Group?
    fun findByExternalId(externalId: String): Group?
    fun list(startIndex: Int, count: Int): Page<Group>
    fun update(group: Group): Group
    fun delete(id: UUID): Boolean
    fun count(): Long

    // Member management
    fun getMembers(groupId: UUID): List<GroupMember>
    fun addMember(groupId: UUID, member: GroupMember): GroupMember
    fun removeMember(groupId: UUID, memberId: UUID): Boolean
    fun setMembers(groupId: UUID, members: List<GroupMember>): List<GroupMember>
}
```

**Rationale**: Domain-oriented interface with pagination support. Separate member management methods for Group to support PATCH operations.

### 3. Page Result Type

Introduce a simple `Page<T>` data class for paginated results:

```kotlin
data class Page<T>(
    val items: List<T>,
    val totalCount: Long,
    val startIndex: Int,
    val itemsPerPage: Int
)
```

**Rationale**: Encapsulates pagination metadata needed for SCIM list responses.

### 4. Entity Design

**GroupEntity**:
```kotlin
@Entity
@Table(name = "revet_groups")
class GroupEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var displayName: String

    @Column(name = "external_id", unique = true)
    var externalId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime
}
```

**GroupMemberEntity**:
```kotlin
@Entity
@Table(name = "revet_group_members")
class GroupMemberEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "group_id", nullable = false)
    lateinit var groupId: UUID

    @Column(name = "member_id", nullable = false)
    lateinit var memberId: UUID

    @Column(name = "member_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var memberType: MemberType = MemberType.USER

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime
}
```

### 5. Resource Modification

Resources become concrete, injecting services:

```kotlin
@Path("/scim/v2/Users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SCIM Users", description = "SCIM 2.0 User management endpoints")
class UserResource(
    private val userService: UserService
) {
    @Context
    lateinit var uriInfo: UriInfo

    @POST
    @Transactional
    fun createUser(scimUser: ScimUser): Response {
        val user = scimUser.toDomain()
        val created = userService.create(user)
        return Response.status(201)
            .entity(created.toScimUser(baseUrl))
            .build()
    }
    // ... other methods
}
```

### 6. SCIM Filter to Repository Query

For initial implementation, support basic filtering by mapping SCIM filters to repository methods:

| SCIM Filter | Repository Method |
|-------------|-------------------|
| `userName eq "x"` | `findByUsername(x)` |
| `externalId eq "x"` | `findByExternalId(x)` |
| `emails.value eq "x"` | `findByEmail(x)` |
| `displayName eq "x"` | `findByDisplayName(x)` (groups) |

Complex filters fall back to in-memory filtering on paginated results.

**Rationale**: Simple approach that covers Okta's primary use cases without building a query translator.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| In-memory filtering for complex queries | Acceptable for initial implementation; add query translation later if needed |
| Circular dependency | `scim` depends on `user-persistence`, not vice versa |
| Large group member lists | Pagination not applied to members; add if needed |

## Migration Plan

No migration needed - this enhances the existing SCIM implementation.

## Open Questions

None - all requirements clarified.

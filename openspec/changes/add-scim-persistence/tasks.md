## 1. Persistence Entities

- [x] 1.1 Create `GroupEntity` in `user-persistence/runtime` with `toDomain()` and `fromDomain()`
- [x] 1.2 Create `GroupMemberEntity` in `user-persistence/runtime` with `toDomain()` and `fromDomain()`

## 2. Repositories

- [x] 2.1 Create `GroupRepository` extending `PanacheRepositoryBase<GroupEntity, UUID>`
- [x] 2.2 Add `findByExternalId(externalId: String)` to `GroupRepository`
- [x] 2.3 Add `findByDisplayName(displayName: String)` to `GroupRepository`
- [x] 2.4 Create `GroupMemberRepository` extending `PanacheRepositoryBase<GroupMemberEntity, UUID>`
- [x] 2.5 Add `findByGroupId(groupId: UUID)` to `GroupMemberRepository`
- [x] 2.6 Add `deleteByGroupId(groupId: UUID)` to `GroupMemberRepository`
- [x] 2.7 Add `findByGroupIdAndMemberId(groupId: UUID, memberId: UUID)` to `GroupMemberRepository`

## 3. Support Types

- [x] 3.1 Create `Page<T>` data class in `user-persistence/runtime` (items, totalCount, startIndex, itemsPerPage)

## 4. User Service

- [x] 4.1 Create `UserService` interface with CRUD methods
- [x] 4.2 Create `UserServiceImpl` implementing `UserService`
- [x] 4.3 Use `IdentityProviderLinkRepository.findByExternalIdAndIdentityProviderId()` for externalId lookups (via existing repository)
- [x] 4.4 Implement `create(user: User, identityProviderId: UUID, externalId: String?)` in `UserServiceImpl`
- [x] 4.5 Implement `findById(id: UUID)` in `UserServiceImpl`
- [x] 4.6 Implement `findByUsername(username: String)` in `UserServiceImpl`
- [x] 4.7 Implement `findByExternalId(externalId: String, identityProviderId: UUID)` in `UserServiceImpl`
- [x] 4.8 Implement `list(startIndex: Int, count: Int)` in `UserServiceImpl`
- [x] 4.9 Implement `update(user: User)` in `UserServiceImpl`
- [x] 4.10 Implement `delete(id: UUID)` in `UserServiceImpl`
- [x] 4.11 Implement `count()` in `UserServiceImpl`
- [x] 4.12 Implement `getExternalId(userId: UUID, identityProviderId: UUID)` in `UserServiceImpl`
- [x] 4.13 Implement `updateExternalId(userId: UUID, identityProviderId: UUID, externalId: String?)` in `UserServiceImpl`

## 5. Group Service

- [x] 5.1 Create `GroupService` interface with CRUD and member methods
- [x] 5.2 Create `GroupServiceImpl` implementing `GroupService`
- [x] 5.3 Implement `create(group: Group)` in `GroupServiceImpl`
- [x] 5.4 Implement `findById(id: UUID)` in `GroupServiceImpl`
- [x] 5.5 Implement `findByExternalId(externalId: String)` in `GroupServiceImpl`
- [x] 5.6 Implement `list(startIndex: Int, count: Int)` in `GroupServiceImpl`
- [x] 5.7 Implement `update(group: Group)` in `GroupServiceImpl`
- [x] 5.8 Implement `delete(id: UUID)` in `GroupServiceImpl`
- [x] 5.9 Implement `count()` in `GroupServiceImpl`
- [x] 5.10 Implement `getMembers(groupId: UUID)` in `GroupServiceImpl`
- [x] 5.11 Implement `addMember(groupId: UUID, member: GroupMember)` in `GroupServiceImpl`
- [x] 5.12 Implement `removeMember(groupId: UUID, memberId: UUID)` in `GroupServiceImpl`
- [x] 5.13 Implement `setMembers(groupId: UUID, members: List<GroupMember>)` in `GroupServiceImpl`
- [x] 5.14 Implement `findByDisplayName(displayName: String)` in `GroupServiceImpl`

## 6. SCIM Module Updates

- [x] 6.1 Add `user-persistence` dependency to `scim/build.gradle.kts`
- [x] 6.2 Modify `UserResource` to be concrete class with `UserService` injection
- [x] 6.3 Implement `createUser()` in `UserResource` using service and mappers
- [x] 6.4 Implement `listUsers()` in `UserResource` with filter and pagination
- [x] 6.5 Implement `getUser()` in `UserResource`
- [x] 6.6 Implement `replaceUser()` in `UserResource`
- [x] 6.7 Implement `patchUser()` in `UserResource`
- [x] 6.8 Implement `deleteUser()` in `UserResource`
- [x] 6.9 Modify `GroupResource` to be concrete class with `GroupService` and `UserService` injection
- [x] 6.10 Implement `createGroup()` in `GroupResource` using service and mappers
- [x] 6.11 Implement `listGroups()` in `GroupResource` with filter and pagination
- [x] 6.12 Implement `getGroup()` in `GroupResource` including members
- [x] 6.13 Implement `replaceGroup()` in `GroupResource` including member replacement
- [x] 6.14 Implement `patchGroup()` in `GroupResource` with add/remove member operations
- [x] 6.15 Implement `deleteGroup()` in `GroupResource`
- [x] 6.16 Create `ScimRequestContext` for passing identity provider ID from filter to resources
- [x] 6.17 Update `BearerTokenFilter` to extract `idpId` from JWT and populate `ScimRequestContext`
- [x] 6.18 Update `UserMapper.toScimUser()` to accept externalId parameter

## 7. SCIM Filter Integration

- [x] 7.1 Create `ScimFilterHelper` to apply `ScimFilter` to service queries
- [x] 7.2 Add filter support to `listUsers()` mapping to service/repository methods
- [x] 7.3 Add filter support to `listGroups()` mapping to service/repository methods

## 8. Testing

- [ ] 8.1 Write unit tests for `GroupEntity.toDomain()` and `fromDomain()`
- [ ] 8.2 Write unit tests for `GroupMemberEntity.toDomain()` and `fromDomain()`
- [ ] 8.3 Write unit tests for `UserServiceImpl`
- [ ] 8.4 Write unit tests for `GroupServiceImpl`
- [ ] 8.5 Write integration tests for `UserResource` with mocked service
- [ ] 8.6 Write integration tests for `GroupResource` with mocked service

# Change: Add SCIM Persistence Layer

## Why

The SCIM API resources (`UserResource`, `GroupResource`) are currently abstract, requiring consumers to implement persistence themselves. This change wires the SCIM implementation to the `user-persistence` module, making the resources concrete and ready-to-use with Panache/Hibernate persistence.

## What Changes

- **New persistence entities**: Add `GroupEntity` and `GroupMemberEntity` to `user-persistence`
- **New repositories**: Add `GroupRepository` and `GroupMemberRepository` to `user-persistence`
- **New service interfaces**: Add `UserService` and `GroupService` interfaces to `user-persistence`
- **New service implementations**: Add `UserServiceImpl` and `GroupServiceImpl` using Panache repositories
- **Modified resources**: Make `UserResource` and `GroupResource` concrete classes that delegate to services
- **Dependency update**: `scim` module depends on `user-persistence`

## Impact

- Affected specs: New `scim-persistence` capability
- Affected code:
  - `user-persistence/runtime/` - new entities, repositories, services
  - `scim/` - modified resources, new dependency on user-persistence

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        scim module                          │
├─────────────────────────────────────────────────────────────┤
│  UserResource ──────► UserService (interface)               │
│  GroupResource ─────► GroupService (interface)              │
│                              │                              │
│  Mappers: User ↔ ScimUser    │                              │
│           Group ↔ ScimGroup  │                              │
└──────────────────────────────┼──────────────────────────────┘
                               │ depends on
┌──────────────────────────────▼──────────────────────────────┐
│                   user-persistence module                    │
├─────────────────────────────────────────────────────────────┤
│  UserServiceImpl ────► UserRepository ────► UserEntity      │
│  GroupServiceImpl ───► GroupRepository ───► GroupEntity     │
│                      ► GroupMemberRepository ► GroupMemberEntity │
└─────────────────────────────────────────────────────────────┘
```

## Key Design Decisions

- **Services work with domain objects**: `UserService` and `GroupService` accept/return `User`, `Group`, `GroupMember` domain objects
- **Mapping happens in resources**: SCIM ↔ Domain conversion happens in the resource layer
- **Entity ↔ Domain in persistence**: `UserEntity.toDomain()` / `fromDomain()` pattern (already established)
- **CDI injection**: Services are `@ApplicationScoped` CDI beans injected into resources

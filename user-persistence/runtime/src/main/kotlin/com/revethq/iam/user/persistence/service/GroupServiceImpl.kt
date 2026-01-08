package com.revethq.iam.user.persistence.service

import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.persistence.Page
import com.revethq.iam.user.persistence.entity.GroupEntity
import com.revethq.iam.user.persistence.entity.GroupMemberEntity
import com.revethq.iam.user.persistence.repository.GroupMemberRepository
import com.revethq.iam.user.persistence.repository.GroupRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class GroupServiceImpl(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository
) : GroupService {

    @Transactional
    override fun create(group: Group): Group {
        val entity = GroupEntity.fromDomain(group)
        groupRepository.persist(entity)
        return entity.toDomain()
    }

    override fun findById(id: UUID): Group? =
        groupRepository.findById(id)?.toDomain()

    override fun findByExternalId(externalId: String): Group? =
        groupRepository.findByExternalId(externalId)?.toDomain()

    override fun findByDisplayName(displayName: String): Group? =
        groupRepository.findByDisplayName(displayName)?.toDomain()

    override fun list(startIndex: Int, count: Int): Page<Group> {
        val total = groupRepository.count()
        val entities = groupRepository.findAll()
            .page(startIndex / count, count)
            .list()
        return Page(
            items = entities.map { it.toDomain() },
            totalCount = total,
            startIndex = startIndex,
            itemsPerPage = count
        )
    }

    @Transactional
    override fun update(group: Group): Group {
        val existing = groupRepository.findById(group.id)
            ?: throw IllegalArgumentException("Group not found: ${group.id}")
        existing.displayName = group.displayName
        existing.externalId = group.externalId
        existing.metadata = group.metadata
        existing.updatedOn = OffsetDateTime.now()
        return existing.toDomain()
    }

    @Transactional
    override fun delete(id: UUID): Boolean {
        groupMemberRepository.deleteByGroupId(id)
        return groupRepository.deleteById(id)
    }

    override fun count(): Long =
        groupRepository.count()

    override fun getMembers(groupId: UUID): List<GroupMember> =
        groupMemberRepository.findByGroupId(groupId).map { it.toDomain() }

    @Transactional
    override fun addMember(groupId: UUID, member: GroupMember): GroupMember {
        val memberWithGroup = member.copy(groupId = groupId)
        val entity = GroupMemberEntity.fromDomain(memberWithGroup)
        groupMemberRepository.persist(entity)
        return entity.toDomain()
    }

    @Transactional
    override fun removeMember(groupId: UUID, memberId: UUID): Boolean {
        val member = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
            ?: return false
        groupMemberRepository.delete(member)
        return true
    }

    @Transactional
    override fun setMembers(groupId: UUID, members: List<GroupMember>): List<GroupMember> {
        groupMemberRepository.deleteByGroupId(groupId)
        return members.map { member ->
            val memberWithGroup = member.copy(groupId = groupId)
            val entity = GroupMemberEntity.fromDomain(memberWithGroup)
            groupMemberRepository.persist(entity)
            entity.toDomain()
        }
    }
}

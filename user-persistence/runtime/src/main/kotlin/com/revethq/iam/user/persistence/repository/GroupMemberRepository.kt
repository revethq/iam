package com.revethq.iam.user.persistence.repository

import com.revethq.iam.user.persistence.entity.GroupMemberEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class GroupMemberRepository : PanacheRepositoryBase<GroupMemberEntity, UUID> {
    fun findByGroupId(groupId: UUID): List<GroupMemberEntity> = list("groupId", groupId)

    fun deleteByGroupId(groupId: UUID): Long = delete("groupId", groupId)

    fun findByGroupIdAndMemberId(
        groupId: UUID,
        memberId: UUID,
    ): GroupMemberEntity? = find("groupId = ?1 and memberId = ?2", groupId, memberId).firstResult()
}

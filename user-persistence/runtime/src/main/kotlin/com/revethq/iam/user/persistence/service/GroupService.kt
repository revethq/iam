package com.revethq.iam.user.persistence.service

import com.revethq.iam.user.domain.Group
import com.revethq.iam.user.domain.GroupMember
import com.revethq.iam.user.persistence.Page
import java.util.UUID

interface GroupService {
    fun create(group: Group): Group

    fun findById(id: UUID): Group?

    fun findByExternalId(externalId: String): Group?

    fun findByDisplayName(displayName: String): Group?

    fun list(
        startIndex: Int,
        count: Int,
    ): Page<Group>

    fun update(group: Group): Group

    fun delete(id: UUID): Boolean

    fun count(): Long

    // Member management
    fun getMembers(groupId: UUID): List<GroupMember>

    fun addMember(
        groupId: UUID,
        member: GroupMember,
    ): GroupMember

    fun removeMember(
        groupId: UUID,
        memberId: UUID,
    ): Boolean

    fun setMembers(
        groupId: UUID,
        members: List<GroupMember>,
    ): List<GroupMember>
}

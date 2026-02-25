package com.revethq.iam.user.domain

import java.time.OffsetDateTime
import java.util.UUID

data class GroupMember(
    var id: UUID? = null,
    var groupId: UUID,
    var memberId: UUID,
    var memberType: MemberType = MemberType.USER,
    var createdOn: OffsetDateTime? = null,
)

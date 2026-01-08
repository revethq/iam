package com.revethq.iam.user.domain

import com.revethq.core.Metadata
import java.time.OffsetDateTime
import java.util.UUID

data class Group(
    var id: UUID,
    var displayName: String,
    var externalId: String? = null,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null
)

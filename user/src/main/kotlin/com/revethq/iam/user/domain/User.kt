package com.revethq.iam.user.domain

import com.revethq.core.Metadata
import java.time.OffsetDateTime
import java.util.UUID

data class User(
    var id: UUID,
    var username: String,
    var email: String,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null
)

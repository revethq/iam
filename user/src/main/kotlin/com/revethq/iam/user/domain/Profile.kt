package com.revethq.iam.user.domain

import java.time.OffsetDateTime
import java.util.UUID

data class Profile(
    var id: UUID? = null,
    var resource: UUID? = null,
    var profileType: ProfileType? = null,
    var profile: Map<String, Any>? = null,
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null
)

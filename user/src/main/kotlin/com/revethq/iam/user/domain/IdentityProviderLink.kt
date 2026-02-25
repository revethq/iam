package com.revethq.iam.user.domain

import com.revethq.core.Metadata
import java.time.OffsetDateTime
import java.util.UUID

data class IdentityProviderLink(
    var id: UUID,
    var userId: UUID,
    var identityProviderId: UUID,
    var externalId: String,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null,
)

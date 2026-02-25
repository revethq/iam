package com.revethq.iam.serviceaccount.domain

import com.revethq.core.Metadata
import java.time.OffsetDateTime
import java.util.UUID

data class ServiceAccount(
    var id: UUID,
    var name: String,
    var description: String? = null,
    var tenantId: String? = null,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null,
) {
    fun toUrn(): String = "urn:revet:iam:${tenantId.orEmpty()}:service-account/$id"
}

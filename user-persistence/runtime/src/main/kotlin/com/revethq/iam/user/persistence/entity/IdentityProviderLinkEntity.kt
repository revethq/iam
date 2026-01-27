package com.revethq.iam.user.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.IdentityProviderLink
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_identity_provider_links")
open class IdentityProviderLinkEntity {

    @Id
    lateinit var id: UUID

    @Column(name = "user_id", nullable = false)
    lateinit var userId: UUID

    @Column(name = "identity_provider_id", nullable = false)
    lateinit var identityProviderId: UUID

    @Column(name = "external_id", nullable = false)
    lateinit var externalId: String

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): IdentityProviderLink = IdentityProviderLink(
        id = id,
        userId = userId,
        identityProviderId = identityProviderId,
        externalId = externalId,
        metadata = metadata,
        createdOn = createdOn,
        updatedOn = updatedOn
    )

    companion object {
        fun fromDomain(link: IdentityProviderLink): IdentityProviderLinkEntity = IdentityProviderLinkEntity().apply {
            val now = OffsetDateTime.now()
            id = link.id
            userId = link.userId
            identityProviderId = link.identityProviderId
            externalId = link.externalId
            metadata = link.metadata
            createdOn = link.createdOn ?: now
            updatedOn = link.updatedOn ?: now
        }
    }
}

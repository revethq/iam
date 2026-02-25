package com.revethq.iam.user.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.IdentityProvider
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_identity_providers")
open class IdentityProviderEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false, unique = true)
    lateinit var name: String

    @Column(name = "external_id", unique = true)
    var externalId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): IdentityProvider =
        IdentityProvider(
            id = id,
            name = name,
            externalId = externalId,
            metadata = metadata,
            createdOn = createdOn,
            updatedOn = updatedOn,
        )

    companion object {
        fun fromDomain(identityProvider: IdentityProvider): IdentityProviderEntity =
            IdentityProviderEntity().apply {
                val now = OffsetDateTime.now()
                id = identityProvider.id
                name = identityProvider.name
                externalId = identityProvider.externalId
                metadata = identityProvider.metadata
                createdOn = identityProvider.createdOn ?: now
                updatedOn = identityProvider.updatedOn ?: now
            }
    }
}

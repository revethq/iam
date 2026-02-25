package com.revethq.iam.serviceaccount.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.serviceaccount.domain.ServiceAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_service_accounts")
open class ServiceAccountEntity {
    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var name: String

    @Column
    var description: String? = null

    @Column(name = "tenant_id")
    var tenantId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): ServiceAccount =
        ServiceAccount(
            id = id,
            name = name,
            description = description,
            tenantId = tenantId,
            metadata = metadata,
            createdOn = createdOn,
            updatedOn = updatedOn,
        )

    companion object {
        fun fromDomain(serviceAccount: ServiceAccount): ServiceAccountEntity =
            ServiceAccountEntity().apply {
                val now = OffsetDateTime.now()
                id = serviceAccount.id
                name = serviceAccount.name
                description = serviceAccount.description
                tenantId = serviceAccount.tenantId
                metadata = serviceAccount.metadata
                createdOn = serviceAccount.createdOn ?: now
                updatedOn = serviceAccount.updatedOn ?: now
            }
    }
}

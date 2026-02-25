package com.revethq.iam.user.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.Group
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_groups")
open class GroupEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "display_name", nullable = false)
    lateinit var displayName: String

    @Column(name = "external_id", unique = true)
    var externalId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): Group =
        Group(
            id = id,
            displayName = displayName,
            externalId = externalId,
            metadata = metadata,
            createdOn = createdOn,
            updatedOn = updatedOn,
        )

    companion object {
        fun fromDomain(group: Group): GroupEntity =
            GroupEntity().apply {
                val now = OffsetDateTime.now()
                id = group.id
                displayName = group.displayName
                externalId = group.externalId
                metadata = group.metadata
                createdOn = group.createdOn ?: now
                updatedOn = group.updatedOn ?: now
            }
    }
}

package com.revethq.iam.user.persistence.entity

import com.revethq.core.Metadata
import com.revethq.iam.user.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_users")
open class UserEntity {

    @Id
    lateinit var id: UUID

    @Column(nullable = false, unique = true)
    lateinit var username: String

    @Column(nullable = false, unique = true)
    lateinit var email: String

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): User = User(
        id = id,
        username = username,
        email = email,
        metadata = metadata,
        createdOn = createdOn,
        updatedOn = updatedOn
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity().apply {
            val now = OffsetDateTime.now()
            id = user.id
            username = user.username
            email = user.email
            metadata = user.metadata
            createdOn = user.createdOn ?: now
            updatedOn = user.updatedOn ?: now
        }
    }
}

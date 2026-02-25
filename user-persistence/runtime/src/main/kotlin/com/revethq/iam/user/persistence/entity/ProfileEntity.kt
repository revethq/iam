package com.revethq.iam.user.persistence.entity

import com.revethq.iam.user.domain.Profile
import com.revethq.iam.user.domain.ProfileType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_profiles")
open class ProfileEntity {
    @Id
    lateinit var id: UUID

    @Column(name = "resource")
    var resource: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false)
    lateinit var profileType: ProfileType

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var profile: Map<String, Any>? = null

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): Profile =
        Profile(
            id = id,
            resource = resource,
            profileType = profileType,
            profile = profile,
            createdOn = createdOn,
            updatedOn = updatedOn,
        )

    companion object {
        fun fromDomain(profile: Profile): ProfileEntity =
            ProfileEntity().apply {
                val now = OffsetDateTime.now()
                id = profile.id ?: UUID.randomUUID()
                resource = profile.resource
                profileType = profile.profileType ?: ProfileType.User
                this.profile = profile.profile
                createdOn = profile.createdOn ?: now
                updatedOn = profile.updatedOn ?: now
            }
    }
}

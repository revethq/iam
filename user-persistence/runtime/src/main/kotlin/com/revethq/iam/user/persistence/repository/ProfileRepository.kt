package com.revethq.iam.user.persistence.repository

import com.revethq.iam.user.domain.ProfileType
import com.revethq.iam.user.persistence.entity.ProfileEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class ProfileRepository : PanacheRepositoryBase<ProfileEntity, UUID> {

    fun findByResource(resource: UUID): List<ProfileEntity> =
        list("resource", resource)

    fun findByProfileType(profileType: ProfileType): List<ProfileEntity> =
        list("profileType", profileType)

    fun findByResourceAndProfileType(resource: UUID, profileType: ProfileType): ProfileEntity? =
        find("resource = ?1 and profileType = ?2", resource, profileType).firstResult()
}

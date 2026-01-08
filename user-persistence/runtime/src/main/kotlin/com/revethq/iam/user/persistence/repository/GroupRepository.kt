package com.revethq.iam.user.persistence.repository

import com.revethq.iam.user.persistence.entity.GroupEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class GroupRepository : PanacheRepositoryBase<GroupEntity, UUID> {

    fun findByExternalId(externalId: String): GroupEntity? =
        find("externalId", externalId).firstResult()

    fun findByDisplayName(displayName: String): GroupEntity? =
        find("displayName", displayName).firstResult()
}

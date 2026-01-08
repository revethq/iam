package com.revethq.iam.user.persistence.repository

import com.revethq.iam.user.persistence.entity.IdentityProviderEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class IdentityProviderRepository : PanacheRepositoryBase<IdentityProviderEntity, UUID> {

    fun findByName(name: String): IdentityProviderEntity? =
        find("name", name).firstResult()

    fun findByExternalId(externalId: String): IdentityProviderEntity? =
        find("externalId", externalId).firstResult()
}

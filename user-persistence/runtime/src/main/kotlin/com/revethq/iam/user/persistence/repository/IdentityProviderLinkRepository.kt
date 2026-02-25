package com.revethq.iam.user.persistence.repository

import com.revethq.iam.user.persistence.entity.IdentityProviderLinkEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class IdentityProviderLinkRepository : PanacheRepositoryBase<IdentityProviderLinkEntity, UUID> {
    fun findByUserId(userId: UUID): List<IdentityProviderLinkEntity> = list("userId", userId)

    fun findByIdentityProviderId(identityProviderId: UUID): List<IdentityProviderLinkEntity> =
        list("identityProviderId", identityProviderId)

    fun findByUserIdAndIdentityProviderId(
        userId: UUID,
        identityProviderId: UUID,
    ): IdentityProviderLinkEntity? = find("userId = ?1 and identityProviderId = ?2", userId, identityProviderId).firstResult()

    fun findByExternalIdAndIdentityProviderId(
        externalId: String,
        identityProviderId: UUID,
    ): IdentityProviderLinkEntity? = find("externalId = ?1 and identityProviderId = ?2", externalId, identityProviderId).firstResult()
}

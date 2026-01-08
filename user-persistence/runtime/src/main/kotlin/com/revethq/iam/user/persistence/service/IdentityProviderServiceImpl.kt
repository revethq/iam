package com.revethq.iam.user.persistence.service

import com.revethq.iam.user.domain.IdentityProvider
import com.revethq.iam.user.persistence.repository.IdentityProviderRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class IdentityProviderServiceImpl(
    private val identityProviderRepository: IdentityProviderRepository
) : IdentityProviderService {

    override fun findById(id: UUID): IdentityProvider? =
        identityProviderRepository.findById(id)?.toDomain()

    override fun findByExternalId(externalId: String): IdentityProvider? =
        identityProviderRepository.findByExternalId(externalId)?.toDomain()

    override fun findByName(name: String): IdentityProvider? =
        identityProviderRepository.findByName(name)?.toDomain()
}

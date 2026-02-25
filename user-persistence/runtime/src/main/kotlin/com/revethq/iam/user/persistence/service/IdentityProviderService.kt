package com.revethq.iam.user.persistence.service

import com.revethq.iam.user.domain.IdentityProvider
import java.util.UUID

interface IdentityProviderService {
    fun findById(id: UUID): IdentityProvider?

    fun findByExternalId(externalId: String): IdentityProvider?

    fun findByName(name: String): IdentityProvider?
}

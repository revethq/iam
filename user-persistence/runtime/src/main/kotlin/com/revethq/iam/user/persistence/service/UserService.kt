package com.revethq.iam.user.persistence.service

import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.Page
import java.util.UUID

interface UserService {
    fun create(
        user: User,
        identityProviderId: UUID,
        externalId: String?,
    ): User

    fun findById(id: UUID): User?

    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?

    fun findByExternalId(
        externalId: String,
        identityProviderId: UUID,
    ): User?

    fun getExternalId(
        userId: UUID,
        identityProviderId: UUID,
    ): String?

    fun list(
        startIndex: Int,
        count: Int,
    ): Page<User>

    fun update(user: User): User

    fun updateExternalId(
        userId: UUID,
        identityProviderId: UUID,
        externalId: String?,
    )

    fun delete(id: UUID): Boolean

    fun count(): Long
}

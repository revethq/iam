package com.revethq.iam.user.persistence.service

import com.revethq.iam.user.domain.IdentityProviderLink
import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.Page
import com.revethq.iam.user.persistence.entity.IdentityProviderLinkEntity
import com.revethq.iam.user.persistence.entity.UserEntity
import com.revethq.iam.user.persistence.repository.IdentityProviderLinkRepository
import com.revethq.iam.user.persistence.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val identityProviderLinkRepository: IdentityProviderLinkRepository
) : UserService {

    @Transactional
    override fun create(user: User, identityProviderId: UUID, externalId: String?): User {
        val entity = UserEntity.fromDomain(user)
        userRepository.persist(entity)

        if (externalId != null) {
            val link = IdentityProviderLink(
                id = UUID.randomUUID(),
                userId = entity.id,
                identityProviderId = identityProviderId,
                externalId = externalId
            )
            identityProviderLinkRepository.persist(IdentityProviderLinkEntity.fromDomain(link))
        }

        return entity.toDomain()
    }

    override fun findById(id: UUID): User? =
        userRepository.findById(id)?.toDomain()

    override fun findByUsername(username: String): User? =
        userRepository.findByUsername(username)?.toDomain()

    override fun findByEmail(email: String): User? =
        userRepository.findByEmail(email)?.toDomain()

    override fun findByExternalId(externalId: String, identityProviderId: UUID): User? {
        val link = identityProviderLinkRepository.findByExternalIdAndIdentityProviderId(externalId, identityProviderId)
            ?: return null
        return userRepository.findById(link.userId)?.toDomain()
    }

    override fun getExternalId(userId: UUID, identityProviderId: UUID): String? {
        val link = identityProviderLinkRepository.findByUserIdAndIdentityProviderId(userId, identityProviderId)
        return link?.externalId
    }

    override fun list(startIndex: Int, count: Int): Page<User> {
        val total = userRepository.count()
        val entities = userRepository.findAll()
            .page(startIndex / count, count)
            .list()
        return Page(
            items = entities.map { it.toDomain() },
            totalCount = total,
            startIndex = startIndex,
            itemsPerPage = count
        )
    }

    @Transactional
    override fun update(user: User): User {
        val existing = userRepository.findById(user.id)
            ?: throw IllegalArgumentException("User not found: ${user.id}")
        existing.username = user.username
        existing.email = user.email
        existing.metadata = user.metadata
        existing.updatedOn = java.time.OffsetDateTime.now()
        return existing.toDomain()
    }

    @Transactional
    override fun updateExternalId(userId: UUID, identityProviderId: UUID, externalId: String?) {
        val existingLink = identityProviderLinkRepository.findByUserIdAndIdentityProviderId(userId, identityProviderId)

        if (externalId == null) {
            existingLink?.let { identityProviderLinkRepository.delete(it) }
        } else if (existingLink != null) {
            existingLink.externalId = externalId
            existingLink.updatedOn = java.time.OffsetDateTime.now()
        } else {
            val link = IdentityProviderLink(
                id = UUID.randomUUID(),
                userId = userId,
                identityProviderId = identityProviderId,
                externalId = externalId
            )
            identityProviderLinkRepository.persist(IdentityProviderLinkEntity.fromDomain(link))
        }
    }

    @Transactional
    override fun delete(id: UUID): Boolean =
        userRepository.deleteById(id)

    override fun count(): Long =
        userRepository.count()
}

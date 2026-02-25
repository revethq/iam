package com.revethq.iam.serviceaccount.persistence.service

import com.revethq.iam.serviceaccount.domain.ServiceAccount
import com.revethq.iam.serviceaccount.persistence.Page
import com.revethq.iam.serviceaccount.persistence.entity.ServiceAccountEntity
import com.revethq.iam.serviceaccount.persistence.repository.ServiceAccountRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class ServiceAccountServiceImpl(
    private val serviceAccountRepository: ServiceAccountRepository
) : ServiceAccountService {

    @Transactional
    override fun create(serviceAccount: ServiceAccount): ServiceAccount {
        val entity = ServiceAccountEntity.fromDomain(serviceAccount)
        serviceAccountRepository.persist(entity)
        return entity.toDomain()
    }

    override fun findById(id: UUID): ServiceAccount? =
        serviceAccountRepository.findById(id)?.toDomain()

    override fun list(startIndex: Int, count: Int): Page<ServiceAccount> {
        val total = serviceAccountRepository.count()
        val entities = serviceAccountRepository.findAll()
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
    override fun update(serviceAccount: ServiceAccount): ServiceAccount {
        val existing = serviceAccountRepository.findById(serviceAccount.id)
            ?: throw IllegalArgumentException("ServiceAccount not found: ${serviceAccount.id}")
        existing.name = serviceAccount.name
        existing.description = serviceAccount.description
        existing.tenantId = serviceAccount.tenantId
        existing.metadata = serviceAccount.metadata
        existing.updatedOn = OffsetDateTime.now()
        return existing.toDomain()
    }

    @Transactional
    override fun delete(id: UUID): Boolean =
        serviceAccountRepository.deleteById(id)

    override fun count(): Long =
        serviceAccountRepository.count()
}

package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.persistence.Page
import com.revethq.iam.permission.persistence.entity.PolicyEntity
import com.revethq.iam.permission.persistence.repository.PolicyAttachmentRepository
import com.revethq.iam.permission.persistence.repository.PolicyRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@ApplicationScoped
class PolicyServiceImpl(
    private val policyRepository: PolicyRepository,
    private val policyAttachmentRepository: PolicyAttachmentRepository
) : PolicyService {

    @Transactional
    override fun create(policy: Policy): Policy {
        val entity = PolicyEntity.fromDomain(policy)
        policyRepository.persist(entity)
        return entity.toDomain()
    }

    override fun findById(id: UUID): Policy? =
        policyRepository.findById(id)?.toDomain()

    override fun findByName(name: String, tenantId: String?): Policy? =
        policyRepository.findByNameAndTenantId(name, tenantId)?.toDomain()

    override fun list(startIndex: Int, count: Int, tenantId: String?): Page<Policy> {
        val total = count(tenantId)
        val pageNumber = if (count > 0) startIndex / count else 0
        val entities = if (tenantId != null) {
            policyRepository.find("tenantId", tenantId)
                .page(pageNumber, count)
                .list()
        } else {
            policyRepository.findAll()
                .page(pageNumber, count)
                .list()
        }
        return Page(
            items = entities.map { it.toDomain() },
            totalCount = total,
            startIndex = startIndex,
            itemsPerPage = count
        )
    }

    @Transactional
    override fun update(policy: Policy): Policy {
        val existing = policyRepository.findById(policy.id)
            ?: throw IllegalArgumentException("Policy not found: ${policy.id}")
        existing.name = policy.name
        existing.description = policy.description
        existing.version = policy.version
        existing.statementsJson = PolicyEntity.fromDomain(policy).statementsJson
        existing.tenantId = policy.tenantId
        existing.metadata = policy.metadata
        existing.updatedOn = OffsetDateTime.now()
        return existing.toDomain()
    }

    @Transactional
    override fun delete(id: UUID): Boolean {
        policyAttachmentRepository.deleteByPolicyId(id)
        return policyRepository.deleteById(id)
    }

    override fun count(tenantId: String?): Long =
        if (tenantId != null) {
            policyRepository.count("tenantId", tenantId)
        } else {
            policyRepository.count()
        }
}

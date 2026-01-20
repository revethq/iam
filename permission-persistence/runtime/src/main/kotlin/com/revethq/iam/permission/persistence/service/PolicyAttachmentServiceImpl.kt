package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.PolicyAttachment
import com.revethq.iam.permission.persistence.entity.PolicyAttachmentEntity
import com.revethq.iam.permission.persistence.repository.PolicyAttachmentRepository
import com.revethq.iam.permission.persistence.repository.PolicyRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

@ApplicationScoped
class PolicyAttachmentServiceImpl(
    private val policyAttachmentRepository: PolicyAttachmentRepository,
    private val policyRepository: PolicyRepository
) : PolicyAttachmentService {

    @Transactional
    override fun attach(policyId: UUID, principalUrn: String, attachedBy: String?): PolicyAttachment {
        val existing = policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn)
        if (existing != null) {
            throw IllegalStateException("Policy $policyId is already attached to principal $principalUrn")
        }

        val attachment = PolicyAttachment(
            id = UUID.randomUUID(),
            policyId = policyId,
            principalUrn = principalUrn,
            attachedBy = attachedBy
        )
        val entity = PolicyAttachmentEntity.fromDomain(attachment)
        policyAttachmentRepository.persist(entity)
        return entity.toDomain()
    }

    @Transactional
    override fun detach(policyId: UUID, principalUrn: String): Boolean =
        policyAttachmentRepository.deleteByPolicyIdAndPrincipalUrn(policyId, principalUrn)

    override fun listAttachmentsForPolicy(policyId: UUID): List<PolicyAttachment> =
        policyAttachmentRepository.findByPolicyId(policyId).map { it.toDomain() }

    override fun listPoliciesForPrincipal(principalUrn: String): List<Policy> {
        val attachments = policyAttachmentRepository.findByPrincipalUrn(principalUrn)
        return attachments.mapNotNull { attachment ->
            policyRepository.findById(attachment.policyId)?.toDomain()
        }
    }

    override fun isAttached(policyId: UUID, principalUrn: String): Boolean =
        policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn) != null
}

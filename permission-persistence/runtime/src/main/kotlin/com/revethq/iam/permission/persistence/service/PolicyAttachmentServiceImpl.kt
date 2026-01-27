package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.PolicyAttachment
import com.revethq.iam.permission.persistence.entity.PolicyAttachmentEntity
import com.revethq.iam.permission.persistence.repository.PolicyAttachmentRepository
import com.revethq.iam.permission.persistence.repository.PolicyRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.jboss.logging.Logger
import java.util.UUID

@ApplicationScoped
class PolicyAttachmentServiceImpl(
    private val policyAttachmentRepository: PolicyAttachmentRepository,
    private val policyRepository: PolicyRepository
) : PolicyAttachmentService {

    private val log = Logger.getLogger(PolicyAttachmentServiceImpl::class.java)

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

    override fun findById(attachmentId: UUID): PolicyAttachment? =
        policyAttachmentRepository.findById(attachmentId)?.toDomain()

    @Transactional
    override fun detach(policyId: UUID, attachmentId: UUID): Boolean {
        val attachment = policyAttachmentRepository.findById(attachmentId)
            ?: return false
        if (attachment.policyId != policyId) {
            return false
        }
        return policyAttachmentRepository.deleteById(attachmentId)
    }

    override fun listAttachmentsForPolicy(policyId: UUID): List<PolicyAttachment> =
        policyAttachmentRepository.findByPolicyId(policyId).map { it.toDomain() }

    override fun listPoliciesForPrincipal(principalUrn: String): List<Policy> {
        log.info("Looking up policies for principal: $principalUrn")
        val attachments = policyAttachmentRepository.findByPrincipalUrn(principalUrn)
        log.info("Found ${attachments.size} attachments for principal")
        val policies = attachments.mapNotNull { attachment ->
            log.info("Loading policy: ${attachment.policyId}")
            policyRepository.findById(attachment.policyId)?.toDomain()
        }
        log.info("Loaded ${policies.size} policies")
        return policies
    }

    override fun listAttachedPoliciesForPrincipal(principalUrn: String): List<AttachedPolicy> {
        log.info("Looking up attached policies for principal: $principalUrn")
        val attachments = policyAttachmentRepository.findByPrincipalUrn(principalUrn)
        log.info("Found ${attachments.size} attachments for principal")
        return attachments.mapNotNull { attachment ->
            val policy = policyRepository.findById(attachment.policyId)?.toDomain()
            if (policy != null) {
                AttachedPolicy(
                    attachmentId = attachment.id,
                    policy = policy,
                    attachedOn = attachment.attachedOn,
                    attachedBy = attachment.attachedBy
                )
            } else {
                null
            }
        }
    }

    override fun isAttached(policyId: UUID, principalUrn: String): Boolean =
        policyAttachmentRepository.findByPolicyIdAndPrincipalUrn(policyId, principalUrn) != null
}

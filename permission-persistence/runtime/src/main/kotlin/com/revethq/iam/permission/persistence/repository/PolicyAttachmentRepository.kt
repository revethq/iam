package com.revethq.iam.permission.persistence.repository

import com.revethq.iam.permission.persistence.entity.PolicyAttachmentEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PolicyAttachmentRepository : PanacheRepositoryBase<PolicyAttachmentEntity, UUID> {
    fun findByPolicyId(policyId: UUID): List<PolicyAttachmentEntity> = find("policyId", policyId).list()

    fun findByPrincipalUrn(principalUrn: String): List<PolicyAttachmentEntity> = find("principalUrn", principalUrn).list()

    fun findByPolicyIdAndPrincipalUrn(
        policyId: UUID,
        principalUrn: String,
    ): PolicyAttachmentEntity? = find("policyId = ?1 and principalUrn = ?2", policyId, principalUrn).firstResult()

    fun deleteByPolicyId(policyId: UUID): Long = delete("policyId", policyId)

    fun deleteByPolicyIdAndPrincipalUrn(
        policyId: UUID,
        principalUrn: String,
    ): Boolean = delete("policyId = ?1 and principalUrn = ?2", policyId, principalUrn) > 0
}

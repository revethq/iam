package com.revethq.iam.permission.persistence.repository

import com.revethq.iam.permission.persistence.entity.PolicyEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PolicyRepository : PanacheRepositoryBase<PolicyEntity, UUID> {
    fun findByName(name: String): PolicyEntity? = find("name", name).firstResult()

    fun findByNameAndTenantId(
        name: String,
        tenantId: String?,
    ): PolicyEntity? =
        if (tenantId != null) {
            find("name = ?1 and tenantId = ?2", name, tenantId).firstResult()
        } else {
            find("name = ?1 and tenantId is null", name).firstResult()
        }

    fun findByTenantId(tenantId: String?): List<PolicyEntity> =
        if (tenantId != null) {
            find("tenantId", tenantId).list()
        } else {
            find("tenantId is null").list()
        }
}

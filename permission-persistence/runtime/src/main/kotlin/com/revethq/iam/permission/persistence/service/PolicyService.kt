package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.persistence.Page
import java.util.UUID

interface PolicyService {
    fun create(policy: Policy): Policy
    fun findById(id: UUID): Policy?
    fun findByName(name: String, tenantId: String? = null): Policy?
    fun list(startIndex: Int, count: Int, tenantId: String? = null): Page<Policy>
    fun update(policy: Policy): Policy
    fun delete(id: UUID): Boolean
    fun count(tenantId: String? = null): Long
}

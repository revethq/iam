package com.revethq.iam.serviceaccount.persistence.service

import com.revethq.iam.serviceaccount.domain.ServiceAccount
import com.revethq.iam.serviceaccount.persistence.Page
import java.util.UUID

interface ServiceAccountService {
    fun create(serviceAccount: ServiceAccount): ServiceAccount

    fun findById(id: UUID): ServiceAccount?

    fun list(
        startIndex: Int,
        count: Int,
    ): Page<ServiceAccount>

    fun update(serviceAccount: ServiceAccount): ServiceAccount

    fun delete(id: UUID): Boolean

    fun count(): Long
}

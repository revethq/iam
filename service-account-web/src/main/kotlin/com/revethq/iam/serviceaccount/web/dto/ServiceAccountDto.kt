package com.revethq.iam.serviceaccount.web.dto

import com.revethq.iam.serviceaccount.domain.ServiceAccount
import java.time.OffsetDateTime
import java.util.UUID

data class CreateServiceAccountRequest(
    val name: String,
    val description: String? = null,
    val tenantId: String? = null
) {
    fun toDomain(): ServiceAccount = ServiceAccount(
        id = UUID.randomUUID(),
        name = name,
        description = description,
        tenantId = tenantId
    )
}

data class UpdateServiceAccountRequest(
    val name: String,
    val description: String? = null,
    val tenantId: String? = null
)

data class ServiceAccountResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val tenantId: String?,
    val createdOn: OffsetDateTime?,
    val updatedOn: OffsetDateTime?
) {
    companion object {
        fun fromDomain(serviceAccount: ServiceAccount): ServiceAccountResponse = ServiceAccountResponse(
            id = serviceAccount.id,
            name = serviceAccount.name,
            description = serviceAccount.description,
            tenantId = serviceAccount.tenantId,
            createdOn = serviceAccount.createdOn,
            updatedOn = serviceAccount.updatedOn
        )
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasMore: Boolean
)

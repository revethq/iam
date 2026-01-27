package com.revethq.iam.permission.web.dto

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import java.time.OffsetDateTime
import java.util.UUID

data class StatementDto(
    val sid: String? = null,
    val effect: String,
    val actions: List<String>,
    val resources: List<String>,
    val conditions: Map<String, Map<String, List<String>>>? = null
) {
    fun toDomain(): Statement = Statement(
        sid = sid,
        effect = Effect.valueOf(effect.uppercase()),
        actions = actions,
        resources = resources,
        conditions = conditions ?: emptyMap()
    )

    companion object {
        fun fromDomain(statement: Statement): StatementDto = StatementDto(
            sid = statement.sid,
            effect = statement.effect.name,
            actions = statement.actions,
            resources = statement.resources,
            conditions = statement.conditions.ifEmpty { null }
        )
    }
}

data class CreatePolicyRequest(
    val name: String,
    val description: String? = null,
    val version: String,
    val statements: List<StatementDto>,
    val tenantId: String? = null
) {
    fun toDomain(): Policy = Policy(
        id = UUID.randomUUID(),
        name = name,
        description = description,
        version = version,
        statements = statements.map { it.toDomain() },
        tenantId = tenantId
    )
}

data class UpdatePolicyRequest(
    val name: String,
    val description: String? = null,
    val version: String,
    val statements: List<StatementDto>,
    val tenantId: String? = null
)

data class PolicyResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val version: String,
    val statements: List<StatementDto>,
    val tenantId: String?,
    val createdOn: OffsetDateTime?,
    val updatedOn: OffsetDateTime?
) {
    companion object {
        fun fromDomain(policy: Policy): PolicyResponse = PolicyResponse(
            id = policy.id,
            name = policy.name,
            description = policy.description,
            version = policy.version,
            statements = policy.statements.map { StatementDto.fromDomain(it) },
            tenantId = policy.tenantId,
            createdOn = policy.createdOn,
            updatedOn = policy.updatedOn
        )
    }
}

data class PolicyListResponse(
    val items: List<PolicyResponse>,
    val totalCount: Long,
    val startIndex: Int,
    val itemsPerPage: Int
)

/**
 * Response for a policy with its attachment information.
 */
data class AttachedPolicyResponse(
    val attachmentId: UUID,
    val policy: PolicyResponse,
    val attachedOn: OffsetDateTime?,
    val attachedBy: String?
) {
    companion object {
        fun fromDomain(attached: com.revethq.iam.permission.persistence.service.AttachedPolicy): AttachedPolicyResponse =
            AttachedPolicyResponse(
                attachmentId = attached.attachmentId,
                policy = PolicyResponse.fromDomain(attached.policy),
                attachedOn = attached.attachedOn,
                attachedBy = attached.attachedBy
            )
    }
}

/**
 * Generic paginated response wrapper.
 * Uses hasMore instead of totalElements to avoid expensive COUNT queries.
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasMore: Boolean
)

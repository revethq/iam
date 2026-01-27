package com.revethq.iam.permission.persistence.entity

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.revethq.core.Metadata
import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "revet_policies")
open class PolicyEntity {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var name: String

    @Column
    var description: String? = null

    @Column(nullable = false)
    lateinit var version: String

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "statements", columnDefinition = "jsonb", nullable = false)
    lateinit var statementsJson: List<Map<String, Any>>

    @Column(name = "tenant_id")
    var tenantId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var metadata: Metadata = Metadata()

    @Column(name = "created_on", nullable = false)
    lateinit var createdOn: OffsetDateTime

    @Column(name = "updated_on", nullable = false)
    lateinit var updatedOn: OffsetDateTime

    fun toDomain(): Policy = Policy(
        id = id,
        name = name,
        description = description,
        version = version,
        statements = parseStatements(statementsJson),
        tenantId = tenantId,
        metadata = metadata,
        createdOn = createdOn,
        updatedOn = updatedOn
    )

    companion object {
        private val objectMapper = jacksonObjectMapper()

        fun fromDomain(policy: Policy): PolicyEntity = PolicyEntity().apply {
            val now = OffsetDateTime.now()
            id = policy.id
            name = policy.name
            description = policy.description
            version = policy.version
            statementsJson = serializeStatements(policy.statements)
            tenantId = policy.tenantId
            metadata = policy.metadata
            createdOn = policy.createdOn ?: now
            updatedOn = policy.updatedOn ?: now
        }

        private fun serializeStatements(statements: List<Statement>): List<Map<String, Any>> {
            return statements.map { statement ->
                buildMap {
                    statement.sid?.let { put("sid", it) }
                    put("effect", statement.effect.name)
                    put("actions", statement.actions)
                    put("resources", statement.resources)
                    if (statement.conditions.isNotEmpty()) {
                        put("conditions", statement.conditions)
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun parseStatements(statementsJson: List<Map<String, Any>>): List<Statement> {
            return statementsJson.map { statementMap ->
                Statement(
                    sid = statementMap["sid"] as? String,
                    effect = Effect.valueOf(statementMap["effect"] as String),
                    actions = statementMap["actions"] as List<String>,
                    resources = statementMap["resources"] as List<String>,
                    conditions = (statementMap["conditions"] as? Map<String, Map<String, List<String>>>) ?: emptyMap()
                )
            }
        }
    }
}

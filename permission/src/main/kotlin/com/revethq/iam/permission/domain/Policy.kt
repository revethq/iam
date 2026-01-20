package com.revethq.iam.permission.domain

import com.revethq.core.Metadata
import java.time.OffsetDateTime
import java.util.UUID

/**
 * An IAM policy containing one or more statements that define permissions.
 *
 * @property id Unique identifier for the policy
 * @property name Human-readable name (unique per tenant)
 * @property description Optional description of what the policy does
 * @property version Policy document version (e.g., "2026-01-15")
 * @property statements List of permission statements
 * @property tenantId Tenant/organization ID (null for global policies)
 * @property metadata Extensible metadata
 * @property createdOn When the policy was created
 * @property updatedOn When the policy was last updated
 */
data class Policy(
    var id: UUID,
    var name: String,
    var description: String? = null,
    var version: String,
    var statements: List<Statement>,
    var tenantId: String? = null,
    var metadata: Metadata = Metadata(),
    var createdOn: OffsetDateTime? = null,
    var updatedOn: OffsetDateTime? = null
) {
    init {
        require(statements.isNotEmpty()) { "Policy must have at least one statement" }
    }
}

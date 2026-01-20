package com.revethq.iam.permission.evaluation

import com.revethq.iam.permission.domain.Policy

/**
 * Interface for collecting policies attached to a principal.
 * Implementations handle the retrieval from persistence layer.
 */
interface PolicyCollector {
    /**
     * Collect all policies that apply to the given principal.
     * This includes:
     * - Policies directly attached to the principal
     * - Policies attached to any groups the principal is a member of
     *
     * @param principalUrn URN of the principal
     * @return List of all applicable policies
     */
    fun collectPolicies(principalUrn: String): List<Policy>
}

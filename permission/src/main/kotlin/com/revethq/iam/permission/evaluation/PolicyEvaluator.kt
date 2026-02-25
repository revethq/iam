package com.revethq.iam.permission.evaluation

import com.revethq.iam.permission.condition.ConditionEvaluator
import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service interface for policy evaluation.
 */
interface PolicyEvaluator {
    /**
     * Evaluate an authorization request against all applicable policies.
     *
     * @param request The authorization request
     * @return The authorization result
     */
    fun evaluate(request: AuthorizationRequest): AuthorizationResult
}

/**
 * Default implementation of PolicyEvaluator with real-time evaluation.
 *
 * Evaluation algorithm:
 * 1. Collect all policies attached to the principal (user policies + group policies)
 * 2. Gather all statements from all policies
 * 3. Evaluate each statement against the request
 * 4. Determine final decision:
 *    - If any Deny statement matched → DENY (explicit)
 *    - If any Allow statement matched → ALLOW
 *    - Otherwise → DENY (implicit)
 */
@ApplicationScoped
class DefaultPolicyEvaluator
    @Inject
    constructor(
        private val policyCollector: PolicyCollector,
    ) : PolicyEvaluator {
        override fun evaluate(request: AuthorizationRequest): AuthorizationResult {
            // 1. Collect all policies for the principal
            val policies = policyCollector.collectPolicies(request.principalUrn)

            // 2. Evaluate all statements
            val matchingStatements = mutableListOf<MatchedStatement>()
            val conditionContext = request.toConditionContext()

            for (policy in policies) {
                for (statement in policy.statements) {
                    if (statementMatches(statement, request, conditionContext)) {
                        matchingStatements.add(MatchedStatement(statement, policy.name))
                    }
                }
            }

            // 3. Determine final decision
            return determineDecision(matchingStatements)
        }

        /**
         * Evaluate policies directly (without collecting from principal).
         * Useful for testing or when policies are already known.
         */
        fun evaluateWithPolicies(
            request: AuthorizationRequest,
            policies: List<Policy>,
        ): AuthorizationResult {
            val matchingStatements = mutableListOf<MatchedStatement>()
            val conditionContext = request.toConditionContext()

            for (policy in policies) {
                for (statement in policy.statements) {
                    if (statementMatches(statement, request, conditionContext)) {
                        matchingStatements.add(MatchedStatement(statement, policy.name))
                    }
                }
            }

            return determineDecision(matchingStatements)
        }

        private fun statementMatches(
            statement: Statement,
            request: AuthorizationRequest,
            conditionContext: com.revethq.iam.permission.condition.ConditionContext,
        ): Boolean {
            // Check if action matches
            if (!statement.matchesAction(request.action)) {
                return false
            }

            // Check if resource matches
            if (!statement.matchesResource(request.resourceUrn)) {
                return false
            }

            // Check if all conditions are satisfied
            if (!ConditionEvaluator.evaluate(statement.conditions, conditionContext)) {
                return false
            }

            return true
        }

        private fun determineDecision(matchingStatements: List<MatchedStatement>): AuthorizationResult {
            if (matchingStatements.isEmpty()) {
                return AuthorizationResult.implicitDeny()
            }

            // Check for any explicit Deny
            val denyStatements = matchingStatements.filter { it.statement.effect == Effect.DENY }
            if (denyStatements.isNotEmpty()) {
                return AuthorizationResult.explicitDeny(denyStatements)
            }

            // Check for any Allow
            val allowStatements = matchingStatements.filter { it.statement.effect == Effect.ALLOW }
            if (allowStatements.isNotEmpty()) {
                return AuthorizationResult.allow(allowStatements)
            }

            // No matching statements (shouldn't reach here, but defensive)
            return AuthorizationResult.implicitDeny()
        }
    }

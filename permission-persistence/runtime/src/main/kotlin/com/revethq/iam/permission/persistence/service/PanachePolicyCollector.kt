package com.revethq.iam.permission.persistence.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.evaluation.PolicyCollector
import com.revethq.iam.permission.service.PolicyAttachmentService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class PanachePolicyCollector
    @Inject
    constructor(
        private val policyAttachmentService: PolicyAttachmentService,
    ) : PolicyCollector {
        override fun collectPolicies(principalUrn: String): List<Policy> = policyAttachmentService.listPoliciesForPrincipal(principalUrn)
    }

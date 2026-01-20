package com.revethq.iam.permission.web.filter

import jakarta.enterprise.context.RequestScoped

/**
 * Request-scoped bean that holds authorization context information.
 * This can be injected into resources to access the current principal and tenant.
 */
@RequestScoped
class AuthorizationContext {
    var principalUrn: String? = null
    var tenantId: String? = null
    var sourceIp: String? = null
}

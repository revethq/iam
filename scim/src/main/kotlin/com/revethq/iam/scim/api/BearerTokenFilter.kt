package com.revethq.iam.scim.api

import com.revethq.iam.scim.exception.ScimUnauthorizedException
import com.revethq.iam.user.persistence.service.IdentityProviderService
import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.jwt.JsonWebToken

@Provider
@Priority(Priorities.AUTHENTICATION)
class BearerTokenFilter : ContainerRequestFilter {

    @Inject
    lateinit var jwt: JsonWebToken

    @Inject
    lateinit var scimRequestContext: ScimRequestContext

    @Inject
    lateinit var identityProviderService: IdentityProviderService

    override fun filter(requestContext: ContainerRequestContext) {
        // Skip authentication for ServiceProviderConfig endpoint (often needs to be public)
        val path = requestContext.uriInfo.path
        if (path.endsWith("/ServiceProviderConfig")) {
            return
        }

        val authHeader = requestContext.getHeaderString("Authorization")
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw ScimUnauthorizedException("Missing or invalid bearer token")
        }

        val token = authHeader.substring(BEARER_PREFIX.length).trim()
        if (token.isEmpty()) {
            throw ScimUnauthorizedException("Bearer token is empty")
        }

        // Extract issuer from JWT and look up identity provider
        val issuer = jwt.issuer
        if (issuer.isNullOrBlank()) {
            throw ScimUnauthorizedException("Missing issuer claim in token")
        }

        val identityProvider = identityProviderService.findByExternalId(issuer)
            ?: throw ScimUnauthorizedException("Unknown identity provider: $issuer")

        // Store the identity provider ID in request-scoped context
        scimRequestContext.identityProviderId = identityProvider.id
    }

    companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}

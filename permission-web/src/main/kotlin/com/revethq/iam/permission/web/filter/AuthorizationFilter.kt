package com.revethq.iam.permission.web.filter

import com.revethq.iam.permission.condition.ConditionContext
import com.revethq.iam.permission.evaluation.AuthorizationRequest
import com.revethq.iam.permission.evaluation.PolicyEvaluator
import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider

/**
 * JAX-RS filter that enforces authorization based on @RequiresPermission annotation.
 *
 * This filter:
 * 1. Extracts the principal from SecurityContext
 * 2. Resolves the action and resource from the annotation
 * 3. Evaluates the request against applicable policies
 * 4. Aborts the request with 403 if not authorized
 */
@Provider
@RequiresPermission(action = "", resource = "") // NameBinding marker
@Priority(Priorities.AUTHORIZATION)
class AuthorizationFilter : ContainerRequestFilter {
    @Context
    lateinit var resourceInfo: ResourceInfo

    @Inject
    lateinit var policyEvaluator: PolicyEvaluator

    @Inject
    lateinit var authorizationContext: AuthorizationContext

    override fun filter(requestContext: ContainerRequestContext) {
        val annotation = findAnnotation() ?: return

        val principal = requestContext.securityContext?.userPrincipal
        if (principal == null) {
            requestContext.abortWith(
                Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(mapOf("error" to "unauthorized", "message" to "Authentication required"))
                    .build(),
            )
            return
        }

        val principalUrn = authorizationContext.principalUrn ?: principal.name
        val action = annotation.action
        val resource = resolveResource(annotation.resource, requestContext)

        val conditionContext =
            ConditionContext(
                principalId = principalUrn,
                sourceIp = authorizationContext.sourceIp ?: getClientIp(requestContext),
                requestedAction = action,
                requestedResource = resource,
            )

        val authRequest =
            AuthorizationRequest(
                principalUrn = principalUrn,
                action = action,
                resourceUrn = resource,
                context = conditionContext,
            )

        val result = policyEvaluator.evaluate(authRequest)

        if (result.isDenied()) {
            requestContext.abortWith(
                Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(
                        mapOf(
                            "error" to "forbidden",
                            "message" to "Access denied for action '$action' on resource '$resource'",
                        ),
                    ).build(),
            )
        }
    }

    private fun findAnnotation(): RequiresPermission? {
        // Check method first, then class
        return resourceInfo.resourceMethod?.getAnnotation(RequiresPermission::class.java)
            ?: resourceInfo.resourceClass?.getAnnotation(RequiresPermission::class.java)
    }

    private fun resolveResource(
        pattern: String,
        requestContext: ContainerRequestContext,
    ): String {
        var result = pattern

        // Replace path parameters
        requestContext.uriInfo.pathParameters.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                result = result.replace("{$key}", values[0])
            }
        }

        // Replace query parameters
        requestContext.uriInfo.queryParameters.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                result = result.replace("{$key}", values[0])
            }
        }

        // Replace tenantId from authorization context
        authorizationContext.tenantId?.let {
            result = result.replace("{tenantId}", it)
        }

        return result
    }

    private fun getClientIp(requestContext: ContainerRequestContext): String? {
        // Check common proxy headers first
        return requestContext
            .getHeaderString("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: requestContext.getHeaderString("X-Real-IP")
    }
}

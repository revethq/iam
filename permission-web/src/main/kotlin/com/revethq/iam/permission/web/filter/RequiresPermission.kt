package com.revethq.iam.permission.web.filter

import jakarta.ws.rs.NameBinding

/**
 * Annotation to require permission for accessing a resource.
 *
 * Usage:
 * ```
 * @RequiresPermission(action = "iam:GetUser", resource = "urn:revet:iam:{tenantId}:user/{userId}")
 * fun getUser(@PathParam("userId") userId: String): User
 * ```
 *
 * The resource string supports placeholders that are resolved at runtime:
 * - {tenantId} - extracted from request context
 * - {pathParam} - extracted from path parameters
 * - {queryParam} - extracted from query parameters
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@NameBinding
annotation class RequiresPermission(
    /**
     * The action being performed (e.g., "iam:GetUser", "storage:GetObject")
     */
    val action: String,

    /**
     * The resource URN pattern. Supports placeholders like {userId}, {tenantId}
     */
    val resource: String
)

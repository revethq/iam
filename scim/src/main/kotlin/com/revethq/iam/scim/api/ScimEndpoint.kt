package com.revethq.iam.scim.api

import jakarta.ws.rs.NameBinding

/**
 * Name-binding annotation that scopes SCIM-specific filters (e.g., BearerTokenFilter)
 * to only SCIM resource classes. This prevents SCIM auth and error handling from
 * applying to non-SCIM endpoints.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@NameBinding
annotation class ScimEndpoint

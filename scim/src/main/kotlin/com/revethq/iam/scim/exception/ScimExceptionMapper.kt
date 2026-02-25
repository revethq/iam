package com.revethq.iam.scim.exception

import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class ScimExceptionMapper : ExceptionMapper<ScimException> {
    override fun toResponse(exception: ScimException): Response =
        Response
            .status(exception.status)
            .entity(exception.toScimError())
            .type(MediaType.APPLICATION_JSON)
            .build()
}

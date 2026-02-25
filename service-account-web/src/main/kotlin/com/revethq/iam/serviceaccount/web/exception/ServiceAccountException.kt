package com.revethq.iam.serviceaccount.web.exception

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class ServiceAccountNotFoundException(id: String) : RuntimeException("ServiceAccount not found: $id")

class ServiceAccountConflictException(message: String) : RuntimeException(message)

@Provider
class ServiceAccountNotFoundExceptionMapper : ExceptionMapper<ServiceAccountNotFoundException> {
    override fun toResponse(exception: ServiceAccountNotFoundException): Response =
        Response.status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to exception.message))
            .build()
}

@Provider
class ServiceAccountConflictExceptionMapper : ExceptionMapper<ServiceAccountConflictException> {
    override fun toResponse(exception: ServiceAccountConflictException): Response =
        Response.status(Response.Status.CONFLICT)
            .entity(mapOf("error" to exception.message))
            .build()
}

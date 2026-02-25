package com.revethq.iam.user.web.exception

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class UserNotFoundException(
    id: String,
) : RuntimeException("User not found: $id")

class UserConflictException(
    message: String,
) : RuntimeException(message)

@Provider
class UserNotFoundExceptionMapper : ExceptionMapper<UserNotFoundException> {
    override fun toResponse(exception: UserNotFoundException): Response =
        Response
            .status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to exception.message))
            .build()
}

@Provider
class UserConflictExceptionMapper : ExceptionMapper<UserConflictException> {
    override fun toResponse(exception: UserConflictException): Response =
        Response
            .status(Response.Status.CONFLICT)
            .entity(mapOf("error" to exception.message))
            .build()
}

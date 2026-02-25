package com.revethq.iam.user.web.exception

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class GroupNotFoundException(
    id: String,
) : RuntimeException("Group not found: $id")

class GroupConflictException(
    message: String,
) : RuntimeException(message)

@Provider
class GroupNotFoundExceptionMapper : ExceptionMapper<GroupNotFoundException> {
    override fun toResponse(exception: GroupNotFoundException): Response =
        Response
            .status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to exception.message))
            .build()
}

@Provider
class GroupConflictExceptionMapper : ExceptionMapper<GroupConflictException> {
    override fun toResponse(exception: GroupConflictException): Response =
        Response
            .status(Response.Status.CONFLICT)
            .entity(mapOf("error" to exception.message))
            .build()
}

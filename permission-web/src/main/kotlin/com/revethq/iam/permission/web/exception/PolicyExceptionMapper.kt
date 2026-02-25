package com.revethq.iam.permission.web.exception

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

data class ErrorResponse(
    val error: String,
    val message: String,
)

@Provider
class PolicyExceptionMapper : ExceptionMapper<PolicyException> {
    override fun toResponse(exception: PolicyException): Response {
        val errorType =
            when (exception) {
                is PolicyNotFoundException -> "not_found"
                is PolicyConflictException -> "conflict"
                is PolicyAttachmentConflictException -> "conflict"
                else -> "error"
            }
        return Response
            .status(exception.statusCode)
            .entity(ErrorResponse(errorType, exception.message ?: "Unknown error"))
            .build()
    }
}

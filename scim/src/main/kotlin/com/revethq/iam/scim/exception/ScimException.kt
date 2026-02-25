package com.revethq.iam.scim.exception

import com.revethq.iam.scim.dtos.ScimError

open class ScimException(
    val status: Int,
    val scimType: String? = null,
    override val message: String? = null,
) : RuntimeException(message) {
    fun toScimError(): ScimError =
        ScimError(
            status = status.toString(),
            scimType = scimType,
            detail = message,
        )
}

class ScimNotFoundException(
    resource: String,
    id: String,
) : ScimException(
        status = 404,
        scimType = null,
        message = "$resource with id '$id' not found",
    )

class ScimBadRequestException(
    scimType: String,
    message: String,
) : ScimException(
        status = 400,
        scimType = scimType,
        message = message,
    )

class ScimConflictException(
    message: String,
) : ScimException(
        status = 409,
        scimType = ScimError.UNIQUENESS,
        message = message,
    )

class ScimUnauthorizedException(
    message: String = "Missing or invalid bearer token",
) : ScimException(
        status = 401,
        scimType = null,
        message = message,
    )

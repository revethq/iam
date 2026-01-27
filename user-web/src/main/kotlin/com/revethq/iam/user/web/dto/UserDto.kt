package com.revethq.iam.user.web.dto

import com.revethq.iam.user.domain.User
import java.time.OffsetDateTime
import java.util.UUID

data class CreateUserRequest(
    val username: String,
    val email: String
) {
    fun toDomain(): User = User(
        id = UUID.randomUUID(),
        username = username,
        email = email
    )
}

data class UpdateUserRequest(
    val username: String,
    val email: String
)

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val createdOn: OffsetDateTime?,
    val updatedOn: OffsetDateTime?
) {
    companion object {
        fun fromDomain(user: User): UserResponse = UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            createdOn = user.createdOn,
            updatedOn = user.updatedOn
        )
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasMore: Boolean
)

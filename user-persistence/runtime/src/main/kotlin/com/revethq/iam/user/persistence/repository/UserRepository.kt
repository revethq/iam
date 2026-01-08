package com.revethq.iam.user.persistence.repository

import com.revethq.iam.user.persistence.entity.UserEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class UserRepository : PanacheRepositoryBase<UserEntity, UUID> {

    fun findByUsername(username: String): UserEntity? =
        find("username", username).firstResult()

    fun findByEmail(email: String): UserEntity? =
        find("email", email).firstResult()
}

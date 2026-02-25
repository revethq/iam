package com.revethq.iam.serviceaccount.persistence.repository

import com.revethq.iam.serviceaccount.persistence.entity.ServiceAccountEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class ServiceAccountRepository : PanacheRepositoryBase<ServiceAccountEntity, UUID>

package com.revethq.iam.serviceaccount.web.api

import com.revethq.iam.serviceaccount.persistence.service.ServiceAccountService
import com.revethq.iam.serviceaccount.web.dto.CreateServiceAccountRequest
import com.revethq.iam.serviceaccount.web.dto.PageResponse
import com.revethq.iam.serviceaccount.web.dto.ServiceAccountResponse
import com.revethq.iam.serviceaccount.web.dto.UpdateServiceAccountRequest
import com.revethq.iam.serviceaccount.web.exception.ServiceAccountNotFoundException
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.UUID

@Path("/service-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ServiceAccountResource {

    @Inject
    lateinit var serviceAccountService: ServiceAccountService

    @POST
    fun createServiceAccount(request: CreateServiceAccountRequest): Response {
        val serviceAccount = request.toDomain()
        val created = serviceAccountService.create(serviceAccount)

        return Response.status(Response.Status.CREATED)
            .entity(ServiceAccountResponse.fromDomain(created))
            .build()
    }

    @GET
    @Path("/{id}")
    fun getServiceAccount(@PathParam("id") id: String): ServiceAccountResponse {
        val uuid = UUID.fromString(id)
        val serviceAccount = serviceAccountService.findById(uuid)
            ?: throw ServiceAccountNotFoundException(id)
        return ServiceAccountResponse.fromDomain(serviceAccount)
    }

    @GET
    fun listServiceAccounts(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("size") @DefaultValue("20") size: Int
    ): PageResponse<ServiceAccountResponse> {
        val result = serviceAccountService.list(page * size, size + 1)
        val hasMore = result.items.size > size
        val content = result.items.take(size)

        return PageResponse(
            content = content.map { ServiceAccountResponse.fromDomain(it) },
            page = page,
            size = size,
            hasMore = hasMore
        )
    }

    @PUT
    @Path("/{id}")
    fun updateServiceAccount(
        @PathParam("id") id: String,
        request: UpdateServiceAccountRequest
    ): ServiceAccountResponse {
        val uuid = UUID.fromString(id)
        val existing = serviceAccountService.findById(uuid)
            ?: throw ServiceAccountNotFoundException(id)

        val updated = existing.copy(
            name = request.name,
            description = request.description,
            tenantId = request.tenantId
        )

        val saved = serviceAccountService.update(updated)
        return ServiceAccountResponse.fromDomain(saved)
    }

    @DELETE
    @Path("/{id}")
    fun deleteServiceAccount(@PathParam("id") id: String): Response {
        val uuid = UUID.fromString(id)
        if (!serviceAccountService.delete(uuid)) {
            throw ServiceAccountNotFoundException(id)
        }
        return Response.noContent().build()
    }
}

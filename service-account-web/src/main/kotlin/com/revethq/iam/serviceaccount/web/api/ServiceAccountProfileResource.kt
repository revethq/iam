package com.revethq.iam.serviceaccount.web.api

import com.revethq.iam.serviceaccount.persistence.service.ServiceAccountService
import com.revethq.iam.serviceaccount.web.exception.ServiceAccountNotFoundException
import com.revethq.iam.user.domain.Profile
import com.revethq.iam.user.domain.ProfileType
import com.revethq.iam.user.persistence.entity.ProfileEntity
import com.revethq.iam.user.persistence.repository.ProfileRepository
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.UUID

@Path("/service-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ServiceAccountProfileResource {
    @Inject
    lateinit var serviceAccountService: ServiceAccountService

    @Inject
    lateinit var profileRepository: ProfileRepository

    @GET
    @Path("/{id}/profile")
    fun getProfile(
        @PathParam("id") id: String,
    ): Response {
        val uuid = UUID.fromString(id)
        serviceAccountService.findById(uuid)
            ?: throw ServiceAccountNotFoundException(id)

        val profileEntity =
            profileRepository.findByResourceAndProfileType(uuid, ProfileType.ServiceAccount)
                ?: return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Profile not found for service account: $id"))
                    .build()

        return Response.ok(profileEntity.toDomain().profile).build()
    }

    @PUT
    @Path("/{id}/profile")
    @Transactional
    fun setProfile(
        @PathParam("id") id: String,
        profile: Map<String, Any>,
    ): Response {
        val uuid = UUID.fromString(id)
        serviceAccountService.findById(uuid)
            ?: throw ServiceAccountNotFoundException(id)

        val existing = profileRepository.findByResourceAndProfileType(uuid, ProfileType.ServiceAccount)

        if (existing != null) {
            existing.profile = profile
            existing.updatedOn = OffsetDateTime.now()
            return Response.ok(existing.toDomain().profile).build()
        }

        val newProfile =
            Profile(
                resource = uuid,
                profileType = ProfileType.ServiceAccount,
                profile = profile,
            )
        val entity = ProfileEntity.fromDomain(newProfile)
        profileRepository.persist(entity)

        return Response
            .status(Response.Status.CREATED)
            .entity(entity.toDomain().profile)
            .build()
    }
}

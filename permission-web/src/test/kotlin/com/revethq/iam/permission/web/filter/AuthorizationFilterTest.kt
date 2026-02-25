package com.revethq.iam.permission.web.filter

import com.revethq.iam.permission.evaluation.AuthorizationDecision
import com.revethq.iam.permission.evaluation.AuthorizationResult
import com.revethq.iam.permission.evaluation.PolicyEvaluator
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.core.UriInfo
import java.lang.reflect.Method
import java.security.Principal
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorizationFilterTest {
    private val policyEvaluator = mockk<PolicyEvaluator>()
    private val authorizationContext = AuthorizationContext()
    private val resourceInfo = mockk<ResourceInfo>()
    private val requestContext = mockk<ContainerRequestContext>(relaxed = true)
    private val uriInfo = mockk<UriInfo>()
    private val securityContext = mockk<SecurityContext>()
    private val principal = mockk<Principal>()

    private val filter =
        AuthorizationFilter().apply {
            this.policyEvaluator = this@AuthorizationFilterTest.policyEvaluator
            this.authorizationContext = this@AuthorizationFilterTest.authorizationContext
            this.resourceInfo = this@AuthorizationFilterTest.resourceInfo
        }

    @Test
    fun `allow access when PolicyEvaluator returns ALLOW`() {
        setupMocks("iam:GetUser", "urn:revet:iam::user/alice")
        every { principal.name } returns "urn:revet:iam::user/admin"
        every { policyEvaluator.evaluate(any()) } returns AuthorizationResult(AuthorizationDecision.ALLOW)

        filter.filter(requestContext)

        verify(exactly = 0) { requestContext.abortWith(any()) }
    }

    @Test
    fun `deny access when PolicyEvaluator returns DENY`() {
        setupMocks("iam:GetUser", "urn:revet:iam::user/alice")
        every { principal.name } returns "urn:revet:iam::user/admin"
        every { policyEvaluator.evaluate(any()) } returns AuthorizationResult(AuthorizationDecision.DENY)

        val responseSlot = slot<Response>()
        every { requestContext.abortWith(capture(responseSlot)) } returns Unit

        filter.filter(requestContext)

        verify { requestContext.abortWith(any()) }
        assertEquals(403, responseSlot.captured.status)
    }

    @Test
    fun `extract principal from security context`() {
        setupMocks("iam:GetUser", "urn:revet:iam::user/alice")
        every { principal.name } returns "urn:revet:iam::user/admin"
        every { policyEvaluator.evaluate(any()) } returns AuthorizationResult(AuthorizationDecision.ALLOW)

        filter.filter(requestContext)

        verify { policyEvaluator.evaluate(match { it.principalUrn == "urn:revet:iam::user/admin" }) }
    }

    @Test
    fun `build correct AuthorizationRequest from annotation`() {
        setupMocks("iam:DeleteUser", "urn:revet:iam::user/{userId}")
        every { principal.name } returns "urn:revet:iam::user/admin"
        every { uriInfo.pathParameters } returns
            MultivaluedHashMap<String, String>().apply {
                add("userId", "alice")
            }
        every { policyEvaluator.evaluate(any()) } returns AuthorizationResult(AuthorizationDecision.ALLOW)

        filter.filter(requestContext)

        verify {
            policyEvaluator.evaluate(
                match {
                    it.action == "iam:DeleteUser" && it.resourceUrn == "urn:revet:iam::user/alice"
                },
            )
        }
    }

    @Test
    fun `return 401 when no principal`() {
        setupMocksWithoutPrincipal("iam:GetUser", "urn:revet:iam::user/alice")

        val responseSlot = slot<Response>()
        every { requestContext.abortWith(capture(responseSlot)) } returns Unit

        filter.filter(requestContext)

        verify { requestContext.abortWith(any()) }
        assertEquals(401, responseSlot.captured.status)
    }

    private fun setupMocks(
        action: String,
        resource: String,
    ) {
        val annotation = mockk<RequiresPermission>()
        every { annotation.action } returns action
        every { annotation.resource } returns resource

        val method = mockk<Method>()
        every { method.getAnnotation(RequiresPermission::class.java) } returns annotation
        every { resourceInfo.resourceMethod } returns method
        every { resourceInfo.resourceClass } returns null

        every { requestContext.securityContext } returns securityContext
        every { securityContext.userPrincipal } returns principal
        every { requestContext.uriInfo } returns uriInfo
        every { uriInfo.pathParameters } returns MultivaluedHashMap()
        every { uriInfo.queryParameters } returns MultivaluedHashMap()
        every { requestContext.getHeaderString(any()) } returns null
    }

    private fun setupMocksWithoutPrincipal(
        action: String,
        resource: String,
    ) {
        val annotation = mockk<RequiresPermission>()
        every { annotation.action } returns action
        every { annotation.resource } returns resource

        val method = mockk<Method>()
        every { method.getAnnotation(RequiresPermission::class.java) } returns annotation
        every { resourceInfo.resourceMethod } returns method
        every { resourceInfo.resourceClass } returns null

        every { requestContext.securityContext } returns securityContext
        every { securityContext.userPrincipal } returns null
    }
}

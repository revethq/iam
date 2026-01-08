package com.revethq.iam.scim.api

import jakarta.enterprise.context.RequestScoped
import java.util.UUID

@RequestScoped
class ScimRequestContext {
    var identityProviderId: UUID? = null
}

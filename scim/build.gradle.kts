plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":user"))
    api(project(":user-persistence:runtime"))

    api(libs.jakarta.ws.rs.api)
    api(libs.jakarta.annotation.api)
    api(libs.jakarta.json.bind.api)
    api(libs.microprofile.openapi.api)
    api(libs.microprofile.jwt.auth.api)
    api(libs.jakarta.enterprise.cdi.api)
    api(platform(libs.quarkus.bom))
    api(libs.quarkus.core)
    api(libs.jakarta.transaction.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.resteasy.core)
}

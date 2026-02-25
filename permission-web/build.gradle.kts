plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":permission"))
    api(project(":permission-persistence:runtime"))

    implementation(libs.jakarta.ws.rs.api)
    implementation(libs.jakarta.enterprise.cdi.api)
    implementation(libs.jakarta.annotation.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.resteasy.core)
}

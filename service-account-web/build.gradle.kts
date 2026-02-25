plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":service-account"))
    api(project(":service-account-persistence"))
    api(project(":user-persistence:runtime"))
    api(project(":permission-web"))

    implementation(platform(libs.quarkus.bom))
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(libs.jakarta.ws.rs.api)
    implementation(libs.jakarta.enterprise.cdi.api)
    implementation(libs.jakarta.annotation.api)
    implementation(libs.jakarta.transaction.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.resteasy.core)
}

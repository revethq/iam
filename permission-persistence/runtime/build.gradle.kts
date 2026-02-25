plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.quarkus.extension)
}

quarkusExtension {
    deploymentArtifact.set("${project.group}:revet-permission-persistence-deployment:${project.version}")
}

// Workaround for Quarkus issue #49115 - validateExtension does cross-project
// configuration resolution that Gradle 9 disallows.
tasks.named("validateExtension") { enabled = false }

dependencies {
    api(project(":permission"))

    implementation(platform(libs.quarkus.bom))
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(libs.quarkus.jdbc.postgresql)
    implementation(libs.quarkus.flyway)
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
}

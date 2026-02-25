plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":user-persistence:runtime"))

    implementation(platform(libs.quarkus.bom))
    implementation(libs.quarkus.core.deployment)
    implementation(libs.quarkus.arc.deployment)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin.deployment)
    implementation(libs.quarkus.jdbc.postgresql.deployment)
    implementation(libs.quarkus.flyway.deployment)

    testImplementation(kotlin("test"))
}

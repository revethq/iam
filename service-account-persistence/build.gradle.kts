plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":service-account"))

    implementation(platform(libs.quarkus.bom))
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(libs.quarkus.jdbc.postgresql)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
}

plugins {
    kotlin("jvm")
    id("io.quarkus.extension")
}

val quarkusVersion: String by project

dependencies {
    api(project(":user"))

    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${quarkusVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
}

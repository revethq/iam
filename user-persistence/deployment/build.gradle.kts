plugins {
    kotlin("jvm")
}

val quarkusVersion: String by project

dependencies {
    implementation(project(":user-persistence:runtime"))

    implementation(enforcedPlatform("io.quarkus:quarkus-bom:${quarkusVersion}"))
    implementation("io.quarkus:quarkus-core-deployment")
    implementation("io.quarkus:quarkus-arc-deployment")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin-deployment")
    implementation("io.quarkus:quarkus-jdbc-postgresql-deployment")
    implementation("io.quarkus:quarkus-flyway-deployment")

    testImplementation(kotlin("test"))
}

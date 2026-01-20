plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":permission"))
    api(project(":permission-persistence:runtime"))

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.jboss.resteasy:resteasy-core:6.2.7.Final")
}

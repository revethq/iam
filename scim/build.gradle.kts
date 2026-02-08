import org.jboss.jandex.IndexWriter
import org.jboss.jandex.Indexer

plugins {
    kotlin("jvm")
}

buildscript {
    dependencies {
        classpath("io.smallrye:jandex:3.2.0")
    }
}

dependencies {
    // Project dependencies
    api(project(":user"))
    api(project(":user-persistence:runtime"))

    // JAX-RS API
    api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

    // Jakarta Annotations (for @Priority)
    api("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // JSON-B API for property naming
    api("jakarta.json.bind:jakarta.json.bind-api:3.0.0")

    // MicroProfile OpenAPI
    api("org.eclipse.microprofile.openapi:microprofile-openapi-api:3.1.1")

    // MicroProfile JWT API
    api("org.eclipse.microprofile.jwt:microprofile-jwt-auth-api:2.1")

    // CDI API
    api("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")

    // Quarkus core (for @RegisterForReflection)
    val quarkusVersion: String by project
    api(platform("io.quarkus:quarkus-bom:${quarkusVersion}"))
    api("io.quarkus:quarkus-core")

    // Transaction API
    api("jakarta.transaction:jakarta.transaction-api:2.0.1")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.jboss.resteasy:resteasy-core:6.2.7.Final")
}

tasks.register("jandex") {
    description = "Generate Jandex index"
    group = "build"

    dependsOn(tasks.named("classes"))

    doLast {
        val indexer = Indexer()
        val classesDir = layout.buildDirectory.dir("classes/kotlin/main").get().asFile

        classesDir.walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .forEach { classFile ->
                classFile.inputStream().use { indexer.index(it) }
            }

        val index = indexer.complete()
        val metaInfDir = layout.buildDirectory.dir("resources/main/META-INF").get().asFile
        metaInfDir.mkdirs()

        val jandexFile = File(metaInfDir, "jandex.idx")
        jandexFile.outputStream().use { IndexWriter(it).write(index) }

        println("Generated Jandex index with ${index.knownClasses.size} classes")
    }
}

tasks.named("jar") {
    dependsOn("jandex")
}

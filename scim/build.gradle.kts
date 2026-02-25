import org.jboss.jandex.IndexWriter
import org.jboss.jandex.Indexer

plugins {
    alias(libs.plugins.kotlin.jvm)
}

buildscript {
    dependencies {
        classpath("io.smallrye:jandex:3.2.0")
    }
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

tasks.register("jandex") {
    description = "Generate Jandex index"
    group = "build"

    dependsOn(tasks.named("classes"))

    doLast {
        val indexer = Indexer()
        val classesDir =
            layout.buildDirectory
                .dir("classes/kotlin/main")
                .get()
                .asFile

        classesDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .forEach { classFile ->
                classFile.inputStream().use { indexer.index(it) }
            }

        val index = indexer.complete()
        val metaInfDir =
            layout.buildDirectory
                .dir("resources/main/META-INF")
                .get()
                .asFile
        metaInfDir.mkdirs()

        val jandexFile = File(metaInfDir, "jandex.idx")
        jandexFile.outputStream().use { IndexWriter(it).write(index) }

        println("Generated Jandex index with ${index.knownClasses.size} classes")
    }
}

tasks.named("jar") {
    dependsOn("jandex")
}

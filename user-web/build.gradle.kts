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
    api(project(":user"))
    api(project(":user-persistence:runtime"))

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

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

import org.jboss.jandex.IndexWriter
import org.jboss.jandex.Indexer

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.quarkus.extension)
}

buildscript {
    dependencies {
        classpath("io.smallrye:jandex:3.2.0")
    }
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

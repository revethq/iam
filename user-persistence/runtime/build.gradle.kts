import org.jboss.jandex.IndexWriter
import org.jboss.jandex.Indexer

plugins {
    kotlin("jvm")
    id("io.quarkus.extension")
}

buildscript {
    dependencies {
        classpath("io.smallrye:jandex:3.2.0")
    }
}

val quarkusVersion: String by project

quarkusExtension {
    deploymentArtifact.set("${project.group}:revet-user-persistence-deployment:${project.version}")
}

dependencies {
    api(project(":user"))

    implementation(platform("io.quarkus:quarkus-bom:${quarkusVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
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

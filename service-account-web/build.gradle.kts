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

val quarkusVersion: String by project

dependencies {
    api(project(":service-account"))
    api(project(":service-account-persistence"))
    api(project(":user-persistence:runtime"))
    api(project(":permission-web"))

    implementation(platform("io.quarkus:quarkus-bom:${quarkusVersion}"))
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.transaction:jakarta.transaction-api")

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

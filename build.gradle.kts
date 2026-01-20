plugins {
    kotlin("jvm") version "2.1.0" apply false
    id("io.quarkus.extension") version "3.17.5" apply false
    base
    `maven-publish`
    id("org.jreleaser") version "1.22.0"
}

buildscript {
    configurations.classpath {
        resolutionStrategy {
            force("org.eclipse.jgit:org.eclipse.jgit:5.13.3.202401111512-r")
        }
    }
}

allprojects {
    group = "com.revethq.iam"
    version = "0.1.3"

    repositories {
        mavenCentral()
    }
}

subprojects {
    // Skip parent modules that don't have source code
    if (name == "user-persistence" || name == "permission-persistence") return@subprojects

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withJavadocJar()
        withSourcesJar()
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        "api"("com.revethq:revet-core:0.1.0")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                artifactId = "revet-${project.name}"
                from(components["java"])

                pom {
                    name.set("Revet IAM - ${project.name}")
                    description.set("Revet IAM ${project.name} module")
                    url.set("https://github.com/revethq/iam")
                    inceptionYear.set("2025")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("your-id")
                            name.set("Your Name")
                        }
                    }

                    scm {
                        url.set("https://github.com/revethq/iam")
                        connection.set("scm:git:git://github.com/revethq/iam.git")
                        developerConnection.set("scm:git:ssh://git@github.com/revethq/iam.git")
                    }
                }
            }
        }

        repositories {
            maven {
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

jreleaser {
    project {
        links {
            homepage.set("https://github.com/revethq/iam")
        }
    }

    release {
        github {
            overwrite.set(true)
        }
    }

    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("user/build/staging-deploy")
                    stagingRepository("user-persistence/runtime/build/staging-deploy")
                    stagingRepository("user-persistence/deployment/build/staging-deploy")
                    stagingRepository("permission/build/staging-deploy")
                    stagingRepository("permission-persistence/runtime/build/staging-deploy")
                    stagingRepository("permission-persistence/deployment/build/staging-deploy")
                    stagingRepository("permission-web/build/staging-deploy")
                    stagingRepository("scim/build/staging-deploy")
                }
            }
        }
    }
}

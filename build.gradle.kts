plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.quarkus.extension) apply false
    alias(libs.plugins.ktlint) apply false
    base
    `maven-publish`
    alias(libs.plugins.jreleaser)
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
    version = "0.1.14"

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
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        withJavadocJar()
        withSourcesJar()
    }

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        // Version catalog type-safe accessors aren't available in subprojects {} blocks.
        // revet-core version is defined in gradle/libs.versions.toml for reference.
        "api"("com.revethq:revet-core:0.1.0")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                // Use full path for nested modules (e.g., :permission-persistence:runtime -> permission-persistence-runtime)
                artifactId = "revet-${project.path.removePrefix(":").replace(":", "-")}"
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
                    stagingRepository("user-web/build/staging-deploy")
                    stagingRepository("scim/build/staging-deploy")
                    stagingRepository("service-account/build/staging-deploy")
                    stagingRepository("service-account-persistence/build/staging-deploy")
                    stagingRepository("service-account-web/build/staging-deploy")
                }
            }
        }
    }
}

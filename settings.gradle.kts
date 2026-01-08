pluginManagement {
    val quarkusVersion: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("io.quarkus.extension") version quarkusVersion
    }
}

rootProject.name = "iam"

include("user")
include("permission")
include("scim")
include("user-persistence")
include("user-persistence:runtime")
include("user-persistence:deployment")

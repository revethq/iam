plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Parent module for Quarkus extension - no source code here
tasks.jar { enabled = false }

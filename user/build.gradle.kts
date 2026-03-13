plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.revet.core)

    testImplementation(kotlin("test"))
}

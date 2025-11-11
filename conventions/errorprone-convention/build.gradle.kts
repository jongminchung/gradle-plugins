description =
    """
    A Gradle plugin that extends the errorprone plugin with additional configurations and reporting features
    """.trimIndent()

gradlePlugin {
    plugins {
        create("errorprone.convention") {
            implementationClass = "io.github.jongminchung.gradle.convention.ErrorproneConventionPlugin"
            id = "io.github.jongminchung.errorprone.convention"
            displayName = "Gradle plugin enhancing errorprone and nullaway plugin"
            description = description
            tags = listOf("errorprone", "nullaway", "gradle-plugin", "quality", "java", "build-tools")
        }
    }
}

dependencies {
    implementation(libs.gradle.nullaway)
    implementation(libs.gradle.errorprone)
}

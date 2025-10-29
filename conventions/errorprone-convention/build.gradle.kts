gradlePlugin {
    plugins {
        create("errorprone.convention") {
            implementationClass = "io.github.jongmin_chung.gradle.convention.ErrorproneConventionPlugin"
            id = "io.github.jongmin-chung.errorprone.convention"
            displayName = "Gradle plugin enhancing errorprone plugin"
            description =
                "A Gradle plugin that extends the errorprone plugin with additional configurations and reporting features"
            tags = listOf("errorprone", "gradle-plugin", "quality", "java", "build-tools")
        }
    }
}

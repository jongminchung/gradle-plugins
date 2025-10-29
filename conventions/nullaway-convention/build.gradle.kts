gradlePlugin {
    plugins {
        create("nullaway.convention") {
            implementationClass = "io.github.jongmin_chung.gradle.convention.NullawayConventionPlugin"
            id = "io.github.jongmin-chung.nullaway.convention"
            displayName = "Gradle plugin enhancing nullaway plugin"
            description =
                "A Gradle plugin that extends the nullaway plugin with additional configurations and reporting features"
            tags = listOf("nullaway", "gradle-plugin", "quality", "java", "build-tools")
        }
    }
}

gradlePlugin {
    plugins {
        create("errorprone.convention") {
            implementationClass = "io.github.jongmin_chung.gradle.convention.ErrorproneConventionPlugin"
            id = "io.github.jongmin-chung.errorprone.convention"
            displayName = "Gradle plugin enhancing errorprone and nullaway plugin"
            description =
                "A Gradle plugin that extends the errorprone plugin with additional configurations and reporting features"
            tags = listOf("errorprone", "nullaway", "gradle-plugin", "quality", "java", "build-tools")
        }
    }
}

dependencies {
    implementation(libs.gradle.nullaway)
    implementation(libs.gradle.errorprone)
}

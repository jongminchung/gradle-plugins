description =
    """
    A Gradle plugin that simplifies and enhances Spotless project configuration
    """.trimIndent()

gradlePlugin {
    plugins {
        create("spotless.convention") {
            implementationClass = "io.github.jongminchung.gradle.convention.SpotlessConventionPlugin"
            id = "io.github.jongminchung.spotless.convention"
            displayName = "Gradle plugin for streamlined Spotless project setup"
            description = description
            tags = listOf("java", "gradle-plugin", "kotlin", "spotless", "configuration")
        }
    }
}

dependencies {
    implementation(libs.gradle.spotless)
}

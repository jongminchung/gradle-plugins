description =
    """
    A Gradle plugin that simplifies and enhances Java project configuration
    """.trimIndent()

gradlePlugin {
    plugins {
        create("java.convention") {
            implementationClass = "io.github.jongminchung.gradle.convention.JavaConventionPlugin"
            id = "io.github.jongminchung.java.convention"
            displayName = "Gradle plugin for streamlined Java project setup"
            description = description
            tags = listOf("java", "gradle-plugin", "build-tools", "compilation", "configuration", "jvm")
        }
    }
}

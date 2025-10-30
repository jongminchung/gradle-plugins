gradlePlugin {
    plugins {
        create("java.convention") {
            implementationClass = "io.github.jongminchung.gradle.convention.JavaConventionPlugin"
            id = "io.github.jongminchung.java.convention"
            displayName = "Gradle plugin for streamlined Java project setup"
            description = "A Gradle plugin that simplifies and enhances Java project configuration"
            tags = listOf("java", "gradle-plugin", "build-tools", "compilation", "configuration", "jvm")
        }
    }
}

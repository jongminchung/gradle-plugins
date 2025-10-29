gradlePlugin {
    plugins {
        create("java.convention") {
            implementationClass = "io.github.jongmin_chung.gradle.convention.JavaConventionPlugin"
            id = "io.github.jongmin-chung.java.convention"
            displayName = "Gradle plugin for streamlined Java project setup"
            description = "A Gradle plugin that simplifies and enhances Java project configuration"
            tags = listOf("java", "gradle-plugin", "build-tools", "compilation", "configuration", "jvm")
        }
    }
}

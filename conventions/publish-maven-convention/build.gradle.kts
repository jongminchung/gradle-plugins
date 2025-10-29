gradlePlugin {
    plugins {
        create("maven-publish.convention") {
            implementationClass = "io.github.jongmin_chung.gradle.convention.PublishMavenConventionPlugin"
            id = "io.github.jongmin-chung.maven-publish.convention"
            displayName = "Gradle plugin streamlining maven-publish configuration"
            description =
                "A Gradle plugin that enhances and simplifies the configuration of the maven-publish plugin for seamless artifact publishing."
            tags = listOf("maven-publish", "publishing", "maven", "gradle-plugin", "build-tools", "configuration")
        }
    }
}

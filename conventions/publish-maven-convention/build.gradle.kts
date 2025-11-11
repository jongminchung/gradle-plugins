description =
    """
    A Gradle plugin that enhances and simplifies the configuration of the maven-publish plugin for seamless artifact publishing.
    """.trimIndent()

gradlePlugin {
    plugins {
        create("maven-publish.convention") {
            implementationClass = "io.github.jongminchung.gradle.convention.PublishMavenConventionPlugin"
            id = "io.github.jongminchung.maven-publish.convention"
            displayName = "Gradle plugin streamlining maven-publish configuration"
            description = description
            tags = listOf("maven-publish", "publishing", "maven", "gradle-plugin", "build-tools", "configuration")
        }
    }
}

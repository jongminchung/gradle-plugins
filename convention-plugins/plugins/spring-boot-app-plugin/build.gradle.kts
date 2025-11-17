project.description =
    """
    A Gradle plugin that simplifies and enhances the configuration of Spring Boot projects
    """.trimIndent()

gradlePlugin {
    plugins {
        create("spring-boot-app") {
            implementationClass = "io.github.jongminchung.gradle.plugin.SpringBootApplicationPlugin"
            id = "io.github.jongminchung.spring-boot-app"
            displayName = "Gradle plugin for Spring Boot Application"
            description = project.description
            tags = listOf("java", "spring", "spring-boot", "gradle-plugin")
        }
    }
}

dependencies {
    implementation(libs.gradle.spring.boot)
    implementation(libs.gradle.spring.dependency.management)

    implementation(rootProject.projects.springBootConvention)

    implementation(rootProject.projects.jacocoConvention)
    implementation(rootProject.projects.jacocoReportAggregationConvention)

    implementation(rootProject.projects.javaConvention)
    implementation(rootProject.projects.jvmTestSuiteConvention)

    implementation(libs.gradle.errorprone)
    implementation(rootProject.projects.errorproneConvention)

    implementation(rootProject.projects.spotlessConvention)

    implementation(libs.gradle.lombok)
}

gradlePlugin {
    plugins {
        create("spring-boot-app") {
            implementationClass = "io.github.jongmin_chung.gradle.plugin.SpringBootApplicationPlugin"
            id = "io.github.jongmin-chung.spring-boot-app"
            displayName = "Gradle plugin for Spring Boot Application"
            description =
                "A Gradle plugin that simplifies and enhances the configuration of Spring Boot projects"
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

    implementation(rootProject.projects.publishMavenConvention)

    implementation(rootProject.projects.javaConvention)
    implementation(rootProject.projects.jvmTestSuiteConvention)

    implementation(rootProject.projects.errorproneConvention)

    implementation(libs.gradle.lombok)
}

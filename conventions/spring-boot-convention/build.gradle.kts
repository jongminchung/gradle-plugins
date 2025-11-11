gradlePlugin {
    plugins {
        create("spring-boot.convention") {
            implementationClass = "io.github.jongminchung.gradle.convention.SpringBootConventionPlugin"
            id = "io.github.jongminchung.spring-boot.convention"
            displayName = "Gradle plugin for Spring Boot project setup"
            description = "A Gradle plugin that simplifies and enhances the configuration of SpringBoot-based projects"
            tags = listOf("spring", "spring-boot", "gradle-plugin", "build-tools", "configuration", "java", "kotlin")
        }
    }
}

dependencies {
    compileOnly(libs.gradle.spring.boot)
    testImplementation(libs.gradle.spring.boot)
    functionalTestRuntimeOnly(libs.gradle.spring.boot)
}

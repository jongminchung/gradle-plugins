gradlePlugin {
    plugins {
        create("spring-boot.convention") {
            implementationClass = "io.github.jongmin_chung.gradle.convention.SpringBootConventionPlugin"
            id = "io.github.jongmin-chung.spring-boot.convention"
            displayName = "Gradle plugin for Spring Boot project setup"
            description = "A Gradle plugin that simplifies and enhances the configuration of SpringBoot-based projects"
            tags = listOf("spring", "spring-boot", "gradle-plugin", "build-tools", "configuration", "java", "kotlin")
        }
    }
}

dependencies {
    compileOnly(libs.gradle.spring.boot)
}

// configurations.compileOnly.get().isCanBeResolved = true
// tasks.withType<PluginUnderTestMetadata> {
//    pluginClasspath.from(configurations.compileOnly)
// }

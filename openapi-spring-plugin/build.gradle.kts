plugins {
    `java-gradle-plugin`
}

dependencies {
    implementation(libs.gradle.node)
    implementation(libs.gradle.openapi.generator)
}

project.description =
    """
    A Gradle plugin that leverages OpenAPI Generator based on OpenAPI Specification to automatically generate DTO and API interface code for use in Spring Boot Application(or Library).
    """.trimIndent()

gradlePlugin {
    plugins {
        create("openapi-spring") {
            implementationClass = "io.github.jongminchung.gradle.plugin.OpenApiSpringPlugin"
            id = "io.github.jongminchung.openapi-spring"
            displayName = "Gradle plugin for OpenAPI Specification based Spring Boot code generation"
            description = project.description
            tags =
                listOf("openapi-generator", "openapi-spec", "swagger", "java", "spring", "spring-boot", "gradle-plugin")
        }
    }
}

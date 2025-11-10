plugins {
    `kotlin-dsl`
}

group = "io.github.jongminchung"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
}

gradlePlugin {
    plugins {
        create("swaggerMerger") {
            id = "io.github.jongminchung.swagger-merger"
            implementationClass = "SwaggerMergerPlugin"
            displayName = "Swagger Merger Plugin"
            description = "A Gradle plugin to merge Swagger specifications using npx swagger-merger"
        }
    }
}

plugins {
    `kotlin-dsl`
}

group = "io.github.jongmin_chung"

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
            id = "io.github.jongmin-chung.swagger-merger"
            implementationClass = "SwaggerMergerPlugin"
            displayName = "Swagger Merger Plugin"
            description = "A Gradle plugin to merge Swagger specifications using npx swagger-merger"
        }
    }
}

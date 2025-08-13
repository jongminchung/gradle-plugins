plugins {
    `kotlin-dsl`
}

group = "io.github.jongmin_chung"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

gradlePlugin {
    plugins {
        create("apiDocsPlugin") {
            id = "io.github.jongmin-chung.api-docs-plugin"
            implementationClass = "ApiDocsPlugin"
            displayName = "Api Document Plugin"
            description = "Hello world!"
        }
    }
}

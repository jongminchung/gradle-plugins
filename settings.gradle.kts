@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "gradle-plugins"

includeBuild("convention-plugins")

include("spring-boot-app-example")
project(":spring-boot-app-example").projectDir = file("$rootDir/examples/spring-boot-app-example")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal() // For resolving Gradle plugins library dependencies
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

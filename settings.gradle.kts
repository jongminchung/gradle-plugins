@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "gradle-plugins"

includeBuild("convention-plugins")

includeBuild("openapi-spring-plugin")

include("spring-boot-app-example")
project(":spring-boot-app-example").projectDir = file("$rootDir/examples/spring-boot-app-example")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

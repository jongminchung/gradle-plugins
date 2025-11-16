@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "gradle-plugins"

include("spring-boot-app-example")
project(":spring-boot-app-example").projectDir = file("$rootDir/examples/spring-boot-app-example")

includeConvention("jacoco-convention")
includeConvention("jacoco-report-aggregation-convention")
includeConvention("publish-maven-convention")
includeConvention("java-convention")
includeConvention("jvm-test-suite-convention")
includeConvention("spring-boot-convention")
includeConvention("errorprone-convention")

includePlugin("spring-boot-app-plugin")

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

fun includeConvention(
    projectPath: String,
    subdir: String = "conventions",
) {
    include(":$projectPath")
    project(":$projectPath").projectDir = file("$rootDir/$subdir/$projectPath")
}

fun includePlugin(
    projectPath: String,
    subdir: String = "plugins",
) {
    include(":$projectPath")
    project(":$projectPath").projectDir = file("$rootDir/$subdir/$projectPath")
}

rootProject.name = "openapi-spring-plugin"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal() // For resolving Gradle plugins library dependencies
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

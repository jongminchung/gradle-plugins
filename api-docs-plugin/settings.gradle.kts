rootProject.name = "api-docs-plugin"

dependencyResolutionManagement {
    versionCatalogs.create("libs") { from(files("../gradle/libs.versions.toml")) }
}

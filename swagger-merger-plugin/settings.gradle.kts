rootProject.name = "swagger-merger-plugin"

dependencyResolutionManagement {
    versionCatalogs.create("libs") { from(files("../gradle/libs.versions.toml")) }
}

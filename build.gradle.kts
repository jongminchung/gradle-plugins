plugins {
    java
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotless.convention)
}

val versionCatalog = libs

allprojects {
    group = "io.github.jongminchung"

    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "io.github.jongminchung.spotless.convention")

    val jdkVersion = versionCatalog.versions.java.get()
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jdkVersion))
        }
    }
}

val includedSpotlessApply = gradle.includedBuilds.map { it.task(":spotlessApply") }

tasks.register("spotlessApplyAll") {
    group = "formatting"
    description = "Runs spotlessApply for root and included builds."
    dependsOn("spotlessApply")
    dependsOn(includedSpotlessApply)
}

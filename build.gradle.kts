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

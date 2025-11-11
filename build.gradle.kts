@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    java
    `jvm-test-suite`
    `jacoco-report-aggregation`
    alias(libs.plugins.spotless)
    alias(libs.plugins.maven.publish) apply false
}

dependencies {
    allprojects.filter { it.name != "spring-boot-app-example" }.forEach {
        jacocoAggregation(project(":${it.path}"))
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}

// Resolving the issue of not being able to reference the version catalog in allprojects and subprojects scopes
val versionCatalog = libs

allprojects {
    group = "io.github.jongminchung"

    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")

    val jdkVersion = versionCatalog.versions.java.get()
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(jdkVersion))
        }
    }

    spotless {
        java {
            palantirJavaFormat(versionCatalog.versions.palantirJavaFormat.get()).formatJavadoc(true)

            formatAnnotations()
            removeUnusedImports()
            trimTrailingWhitespace()
            importOrder("java", "jakarta", "org", "com", "net", "io", "lombok", "io.github.jongminchung")
        }

        kotlin {
            ktlint()
            trimTrailingWhitespace()
        }

        kotlinGradle {
            ktlint()
            trimTrailingWhitespace()
        }
    }
}

configure(subprojects.filter { !it.name.endsWith("example") }) {
    apply(plugin = "jacoco")
    apply(plugin = "java-gradle-plugin")
    apply(
        plugin =
            versionCatalog.plugins.maven.publish
                .get()
                .pluginId,
    )

    testing {
        suites {
            withType<JvmTestSuite>().configureEach { useJUnitJupiter() }

            val test by getting(JvmTestSuite::class)
            val functionalTest by registering(JvmTestSuite::class) {
                dependencies {
                    implementation(project())
                    implementation(gradleTestKit())
                }

                targets {
                    all { testTask.configure { shouldRunAfter(test) } }
                }
            }
        }
    }

    configurations {
        named<Configuration>("functionalTestImplementation").get().extendsFrom(configurations.testImplementation.get())
        named<Configuration>("functionalTestRuntimeOnly").configure {
            extendsFrom(configurations.testRuntimeOnly.get())
        }
    }

    tasks.named("check") {
        dependsOn(testing.suites.named("functionalTest"))
    }

    configurations {
        testImplementation.get().extendsFrom(compileOnly.get())
    }

    reporting {
        reports {
            configureEach {
                if (this is JacocoCoverageReport) {
                    reportTask {
                        reports {
                            xml.required = true
                            html.required = true
                            csv.required = false
                        }
                    }
                }
            }
        }
    }

    tasks.jacocoTestReport {
        reports {
            xml.required = true
            html.required = true
            csv.required = false
        }
    }

    val functionalTest by sourceSets.existing
    configure<GradlePluginDevelopmentExtension> { testSourceSet(functionalTest.get()) }

    configure<GradlePluginDevelopmentExtension> {
        website = "https://github.com/jongminchung/gradle-plugins"
        vcsUrl = "https://github.com/jongminchung/gradle-plugins.git"
    }

    dependencies {
        implementation(versionCatalog.jspecify)

        testImplementation(gradleKotlinDsl())

        testImplementation(versionCatalog.junit.jupiter)
        testImplementation(versionCatalog.assertj.core)
        testRuntimeOnly(versionCatalog.junit.jupiter.engine)
    }

    /**
     * Default Maven publication configuration for all subprojects
     */
    extensions.configure<MavenPublishBaseExtension> {
        signAllPublications()

        coordinates(
            groupId = project.group.toString(),
            artifactId = project.name,
            version = project.version.toString(),
        )

        pom {
            name = project.name
            description = project.description
            url = "https://github.com/jongminchung/gradle-plugins"

            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }

            developers {
                developer {
                    id = "jongminchung"
                    name = "Jongmin Chung"
                    email = "chungjm0711@gmail.com"
                }
            }

            scm {
                connection = "scm:git:git://github.com/jongminchung/gradle-plugins.git"
                developerConnection = "scm:git:ssh://github.com/jongminchung/gradle-plugins.git"
                url = "https://github.com/jongminchung/gradle-plugins"
            }
        }
    }
}

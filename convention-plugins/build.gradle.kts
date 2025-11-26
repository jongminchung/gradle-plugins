@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.plugins.BasePlugin.ASSEMBLE_TASK_NAME
import org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME
import org.gradle.api.plugins.JavaBasePlugin.BUILD_TASK_NAME
import org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME
import org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME
import org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP

plugins {
    java
    `jacoco-report-aggregation`
    alias(libs.plugins.spotless)
    alias(libs.plugins.maven.publish) apply false
}

dependencies {
    allprojects.forEach {
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
    version = versionCatalog.versions.convention.plugin

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
            importOrder("java", "jakarta", "org", "com", "net", "io", "lombok")

            targetExclude("**/build/**")
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

subprojects {
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

    tasks.named("check") {
        dependsOn(testing.suites.named("functionalTest"))
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
     * ORG_GRADLE_PROJECT_mavenCentralUsername
     * ORG_GRADLE_PROJECT_mavenCentralPassword
     *
     * ORG_GRADLE_PROJECT_signingInMemoryKey
     * ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
     */
    extensions.configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()

        coordinates(
            groupId = project.group.toString(),
            artifactId = project.name,
            version =
                versionCatalog.versions.convention.plugin
                    .get(),
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

var publishToMavenCentralTaskName = "publishToMavenCentral"

listOf(
    publishToMavenCentralTaskName to PUBLISH_TASK_GROUP,
    PUBLISH_LOCAL_LIFECYCLE_TASK_NAME to PUBLISH_TASK_GROUP,
).forEach { (taskName, taskGroup) ->
    tasks.register(taskName) {
        group = taskGroup
        description = "Publishes all of subprojects."
    }
}

listOf(
    CLEAN_TASK_NAME,
    ASSEMBLE_TASK_NAME,
    BUILD_TASK_NAME,
    CHECK_TASK_NAME,
    "jacocoTestReport",
    "testCodeCoverageReport",
    PUBLISH_LIFECYCLE_TASK_NAME,
    PUBLISH_LOCAL_LIFECYCLE_TASK_NAME,
    publishToMavenCentralTaskName,
).forEach { taskName ->
    tasks.findByName(taskName)?.also { task ->
        task.dependsOn(project.subprojects.mapNotNull { it.tasks.findByName(taskName) })
    }
}

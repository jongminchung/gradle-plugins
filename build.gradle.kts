@file:Suppress("UnstableApiUsage")

import org.gradle.api.publish.maven.plugins.MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME
import org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME
import org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
import org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.CLEAN_TASK_NAME

plugins {
    java
    signing
    `jvm-test-suite`
    `jacoco-report-aggregation`
    alias(libs.plugins.spotless)
    alias(libs.plugins.gradle.plugin.publish) apply false
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
        withJavadocJar()
        withSourcesJar()
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
//    apply(plugin = "signing")
    apply(plugin = "com.gradle.plugin-publish")

    testing {
        suites {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
            }

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

//    signing {
//        // ORG_GRADLE_PROJECT_signingKeyId
//        val signingKeyId: String? by project
//        // ascii-armored format
//        // ORG_GRADLE_PROJECT_signingKey
//        val signingKey: String? by project
//        // ORG_GRADLE_PROJECT_signingPassword
//        val signingPassword: String? by project
//
//        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
//        sign(extensions.getByType<PublishingExtension>().publications)
//    }

    dependencies {
        implementation(versionCatalog.jspecify)
    }
}

listOf(
    PUBLISH_LIFECYCLE_TASK_NAME to PUBLISH_TASK_GROUP,
    PUBLISH_LOCAL_LIFECYCLE_TASK_NAME to PUBLISH_TASK_GROUP,
    "publishPlugins" to PUBLISH_TASK_GROUP,
).forEach { (taskName, taskGroup) ->
    tasks.register(taskName) {
        group = taskGroup
        description = "Aggregate task for publishing from all subprojects."
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
    "publishPlugins",
).forEach { taskName ->
    tasks.findByName(taskName)?.also { task ->
        task.dependsOn(project.subprojects.mapNotNull { it.tasks.findByName(taskName) })
    }
}

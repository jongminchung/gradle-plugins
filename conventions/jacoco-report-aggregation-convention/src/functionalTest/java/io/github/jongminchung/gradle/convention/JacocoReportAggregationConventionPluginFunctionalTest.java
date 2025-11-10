package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JacocoReportAggregationConventionPluginFunctionalTest {

    private static final String PLUGIN_ID = "io.github.jongminchung.jacoco-report-aggregation.convention";

    @TempDir
    Path projectDir;

    @Test
    void addsJacocoEnabledProjectsToAggregationConfiguration() throws IOException {
        writeSettings();
        writeRootBuildScript();
        writeAppBuildScript();

        BuildResult result = runGradle("verifyAggregation");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"jacoco-report-agg\"\ninclude(\"app\")");
    }

    private void writeRootBuildScript() throws IOException {
        Files.writeString(
                projectDir.resolve("build.gradle.kts"),
                """
                        plugins {
                            id("%s")
                            jacoco-report-aggregation
                        }

                        tasks.register("verifyAggregation") {
                            doLast {
                                val deps = configurations.getByName("jacocoAggregation").dependencies
                                check(deps.any { it is org.gradle.api.artifacts.ProjectDependency && it.path == ":app" })
                            }
                        }
                        """
                        .formatted(PLUGIN_ID));
    }

    private void writeAppBuildScript() throws IOException {
        Path appDir = projectDir.resolve("app");
        Files.createDirectories(appDir);
        Files.writeString(
                appDir.resolve("build.gradle.kts"),
                """
                        plugins {
                            id("java")
                            jacoco
                        }
                        """);
    }

    private BuildResult runGradle(String... args) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(args)
                .withPluginClasspath()
                .build();
    }
}

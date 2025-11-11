package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ErrorproneConventionPluginFunctionalTest {

    private static final String PLUGIN_ID = "io.github.jongminchung.errorprone.convention";

    @TempDir
    Path projectDir;

    @Test
    void configuresErrorproneDependencies() throws IOException {
        writeSettings();
        writeBuildScript("""
                        plugins {
                            id("%s")
                            id("java")
                        }

                        tasks.register("verifyErrorproneConvention") {
                            doLast {
                                val implementation = configurations.getByName("implementation").dependencies
                                check(implementation.any { it.group == "org.jspecify" && it.name == "jspecify" })

                                val errorprone = configurations.getByName("errorprone").dependencies
                                check(errorprone.any { it.group == "com.uber.nullaway" && it.name == "nullaway" })
                                check(errorprone.any { it.group == "com.google.errorprone" && it.name == "error_prone_core" })
                            }
                        }
                        """.formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifyErrorproneConvention");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(
                projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"errorprone-convention-test\"");
    }

    private void writeBuildScript(String content) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle.kts"), content);
    }

    private BuildResult runGradle(String... args) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(args)
                .withPluginClasspath()
                .build();
    }
}

package io.github.jongminchung.gradle.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpringBootApplicationPluginFunctionalTest {

    private static final String PLUGIN_ID = "io.github.jongminchung.spring-boot-app";

    @TempDir
    Path projectDir;

    @Test
    void appliesAllSupportingPluginsInRealBuild() throws IOException {
        writeSettings();
        writeBuildScript(
                """
                        plugins {
                            id("%s")
                        }

                        tasks.register("verifySpringBootAppPlugin") {
                            doLast {
                                check(pluginManager.hasPlugin("org.springframework.boot"))
                                check(pluginManager.hasPlugin("io.github.jongminchung.spring-boot.convention"))
                                check(pluginManager.hasPlugin("io.github.jongminchung.java.convention"))
                                check(pluginManager.hasPlugin("io.github.jongminchung.jvm-test-suite.convention"))
                            }
                        }
                        """
                        .formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifySpringBootAppPlugin");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"spring-boot-app-plugin\"");
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

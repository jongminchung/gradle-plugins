import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaConventionPluginFunctionalTest {
    private static final String PLUGIN_ID = "io.github.jongminchung.java.convention";

    @TempDir
    Path projectDir;

    @Test
    void exposesJavadocAndSourcesTasksByDefault() throws IOException {
        writeSettings();
        writeBuildScript("""
                        plugins {
                            id("%s")
                            id("java")
                        }

                        tasks.register("verifyJavaConvention") {
                            doLast {
                                require(tasks.findByName("javadocJar") != null) {
                                    "javadocJar task should exist"
                                }
                                require(tasks.findByName("sourcesJar") != null) {
                                    "sourcesJar task should exist"
                                }
                            }
                        }
                        """.formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifyJavaConvention");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    @Test
    void canDisablePluginViaExtension() throws IOException {
        writeSettings();
        writeBuildScript("""
                        plugins {
                            id("%s")
                            id("java")
                        }

                        javaExt {
                            enabled = false
                        }

                        tasks.register("verifyJavaConventionDisabled") {
                            doLast {
                                require(tasks.findByName("javadocJar") == null) {
                                    "javadocJar task should not be registered"
                                }
                                require(tasks.findByName("sourcesJar") == null) {
                                    "sourcesJar task should not be registered"
                                }
                            }
                        }
                        """.formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifyJavaConventionDisabled");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"java-convention-test\"");
    }

    private void writeBuildScript(String content) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle.kts"), content);
    }

    private BuildResult runGradle(String... tasks) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(tasks)
                .withPluginClasspath()
                .build();
    }
}

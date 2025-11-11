package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JvmTestSuiteConventionPluginFunctionalTest {
    private static final String PLUGIN_ID = "io.github.jongminchung.jvm-test-suite.convention";

    @TempDir
    Path projectDir;

    @Test
    void registersAdditionalSuitesInRealBuild() throws IOException {
        writeSettings();
        writeBuildScript(
                """
                        import org.gradle.testing.base.TestingExtension
                        import org.gradle.api.plugins.jvm.JvmTestSuite

                        plugins {
                            id("%s")
                            id("java")
                            id("jvm-test-suite")
                        }

                        tasks.register("verifyJvmTestSuites") {
                            doLast {
                                val testing = project.extensions.getByType(TestingExtension::class)
                                val functional = testing.suites.findByName("functionalTest") as JvmTestSuite?
                                check(functional != null) { "functionalTest suite missing" }

                                val integration = testing.suites.findByName("integrationTest") as JvmTestSuite?
                                check(integration != null) { "integrationTest suite missing" }

                                val functionalImpl = project.configurations.getByName("functionalTestImplementation")
                                check(functionalImpl.extendsFrom.any { it.name == "implementation" }) {
                                    "functionalTestImplementation should extend from implementation"
                                }
                            }
                        }
                        """
                        .formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifyJvmTestSuites");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"jvm-test-suite-convention\"");
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

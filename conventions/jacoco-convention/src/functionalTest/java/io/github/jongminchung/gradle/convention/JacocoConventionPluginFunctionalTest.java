package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JacocoConventionPluginFunctionalTest {
    private static final String PLUGIN_ID = "io.github.jongminchung.jacoco.convention";

    @TempDir
    Path projectDir;

    @Test
    void configuresJacocoReportsViaTask() throws IOException {
        writeSettings();
        writeBuildScript("""
                        import org.gradle.testing.jacoco.tasks.JacocoReport

                        plugins {
                            id("%s")
                            id("java")
                            jacoco
                        }

                        tasks.register("verifyJacocoConvention") {
                            doLast {
                                val report = tasks.withType(JacocoReport::class).named("jacocoTestReport").get()
                                check(report.reports.xml.required.get())
                                check(report.reports.html.required.get())
                                check(!report.reports.csv.required.get())
                            }
                        }
                        """.formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifyJacocoConvention");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"jacoco-convention-test\"");
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

package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PublishMavenConventionPluginFunctionalTest {

    private static final String PLUGIN_ID = "io.github.jongminchung.maven-publish.convention";

    @TempDir
    Path projectDir;

    @Test
    void enablesBuildIdentifiersOnPublications() throws IOException {
        writeSettings();
        writeBuildScript("""
                        import org.gradle.api.publish.internal.PublicationInternal
                        import org.gradle.api.publish.maven.MavenPublication

                        plugins {
                            id("%s")
                            id("java")
                            `maven-publish`
                        }

                        publishing {
                            publications {
                                create<MavenPublication>("recording") {
                                    from(components["java"])
                                }
                            }
                        }

                        tasks.register("verifyMavenPublishConvention") {
                            doLast {
                                val publication =
                                    publishing.publications.getByName("recording") as PublicationInternal<*>
                                check(publication.isPublishBuildId) {
                                    "Build identifier should be enabled on publications"
                                }
                            }
                        }
                        """.formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifyMavenPublishConvention");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"publish-maven-convention\"");
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

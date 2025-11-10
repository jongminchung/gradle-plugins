package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpringBootConventionPluginFunctionalTest {

    private static final String PLUGIN_ID = "io.github.jongminchung.spring-boot.convention";

    @TempDir
    Path projectDir;

    @Test
    void configuresSpringBootDependenciesAndTasks() throws IOException {
        writeSettings();
        writeBuildSrcStub();
        writeBuildScript(
                """
                        import org.springframework.boot.gradle.plugin.SpringBootPlugin
                        import org.gradle.api.artifacts.ModuleDependency

                        plugins {
                            id("%s")
                            id("java")
                        }

                        apply<SpringBootPlugin>()

                        tasks.register("verifySpringBootConvention") {
                            doLast {
                                val implementation = configurations.getByName("implementation").dependencies
                                check(implementation.any { it.group == "org.springframework.boot" && it.name == "spring-boot-starter" })

                                val testImplementation = configurations.getByName("testImplementation").dependencies
                                val starterTest = testImplementation.first { it.group == "org.springframework.boot" && it.name == "spring-boot-starter-test" } as ModuleDependency
                                check(starterTest.excludeRules.any { it.group == "org.junit.vintage" && it.module == "junit-vintage-engine" })

                                val testRuntimeOnly = configurations.getByName("testRuntimeOnly").dependencies
                                check(testRuntimeOnly.any { it.group == "org.junit.platform" && it.name == "junit-platform-launcher" })

                                val compileJava = tasks.named("compileJava").get()
                                val bootBuildInfo = tasks.named("bootBuildInfo").get()
                                check(bootBuildInfo.dependsOn.contains(compileJava))
                                val mustRunAfter = bootBuildInfo.mustRunAfter.getDependencies(bootBuildInfo)
                                check(mustRunAfter.contains(compileJava))
                            }
                        }
                        """
                        .formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifySpringBootConvention");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"spring-boot-convention\"");
    }

    private void writeBuildScript(String content) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle.kts"), content);
    }

    private void writeBuildSrcStub() throws IOException {
        Path buildSrcDir = projectDir.resolve("buildSrc/src/main/java");
        Files.createDirectories(buildSrcDir.resolve("org/springframework/boot/gradle/plugin"));
        Files.createDirectories(buildSrcDir.resolve("org/springframework/boot/gradle/dsl"));

        Path extensionFile = buildSrcDir.resolve("org/springframework/boot/gradle/dsl/SpringBootExtension.java");
        Files.writeString(
                extensionFile,
                """
                        package org.springframework.boot.gradle.dsl;

                        import org.gradle.api.Project;

                        public class SpringBootExtension {
                            private final Project project;

                            public SpringBootExtension(Project project) {
                                this.project = project;
                            }

                            public void buildInfo() {
                                project.getTasks().maybeCreate("bootBuildInfo");
                            }
                        }
                        """,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        Path pluginFile = buildSrcDir.resolve("org/springframework/boot/gradle/plugin/SpringBootPlugin.java");
        Files.writeString(
                pluginFile,
                """
                        package org.springframework.boot.gradle.plugin;

                        import org.gradle.api.Plugin;
                        import org.gradle.api.Project;
                        import org.gradle.api.plugins.JavaPlugin;
                        import org.springframework.boot.gradle.dsl.SpringBootExtension;

                        public class SpringBootPlugin implements Plugin<Project> {
                            @Override
                            public void apply(Project project) {
                                project.getPluginManager().apply(JavaPlugin.class);
                                project.getExtensions().create("springBoot", SpringBootExtension.class, project);
                                project.getTasks().register("bootJar");
                            }
                        }
                        """,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private BuildResult runGradle(String... args) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(args)
                .withPluginClasspath()
                .forwardOutput()
                .build();
    }
}

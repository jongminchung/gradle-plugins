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
                        import org.gradle.api.artifacts.ModuleDependency
                        
                        plugins {
                            id("java")
                            id("%s")
                        }
                        
                        apply(plugin = "org.springframework.boot")
                        
                        tasks.register("verifySpringBootConvention") {
                            doLast {
                                val implementation = configurations.getByName("implementation").dependencies
                                check(implementation.any { it.group == "org.springframework.boot" && it.name == "spring-boot-starter" }) {
                                    "spring-boot-starter not found in implementation"
                                }
                        
                                val testImplementation = configurations.getByName("testImplementation").dependencies
                                val starterTest = testImplementation.firstOrNull {
                                    it.group == "org.springframework.boot" && it.name == "spring-boot-starter-test"
                                } as? ModuleDependency
                                check(starterTest != null) { "spring-boot-starter-test not found" }
                                check(starterTest.excludeRules.any {
                                    it.group == "org.junit.vintage" && it.module == "junit-vintage-engine" 
                                }) { "junit-vintage-engine not excluded" }
                        
                                val testRuntimeOnly = configurations.getByName("testRuntimeOnly").dependencies
                                check(testRuntimeOnly.any { 
                                    it.group == "org.junit.platform" && it.name == "junit-platform-launcher" 
                                }) { "junit-platform-launcher not found in testRuntimeOnly" }
                        
                                val compileJava = tasks.named("compileJava").get()
                                val bootBuildInfo = tasks.named("bootBuildInfo").get()
                        
                                val dependsOn = bootBuildInfo.taskDependencies.getDependencies(bootBuildInfo)
                                check(dependsOn.any { it.name == compileJava.name }) {
                                    "bootBuildInfo does not depend on compileJava"
                                }
                        
                                val mustRunAfter = bootBuildInfo.mustRunAfter.getDependencies(bootBuildInfo)
                                check(mustRunAfter.any { it.name == compileJava.name }) {
                                    "bootBuildInfo does not mustRunAfter compileJava"
                                }
                        
                                println("All checks passed!")
                            }
                        }
                        """
                        .formatted(PLUGIN_ID));

        BuildResult result = runGradle("verifySpringBootConvention", "--stacktrace");
        assertThat(result.getOutput()).contains("All checks passed!");
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    private void writeSettings() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"),
                "rootProject.name = \"spring-boot-convention\"");
    }

    private void writeBuildScript(String content) throws IOException {
        Files.writeString(projectDir.resolve("build.gradle.kts"), content);
    }

    private void writeBuildSrcStub() throws IOException {
        Path buildSrcDir = projectDir.resolve("buildSrc/src/main/java");
        Files.createDirectories(buildSrcDir.resolve("org/springframework/boot/gradle/plugin"));
        Files.createDirectories(buildSrcDir.resolve("org/springframework/boot/gradle/dsl"));

        Path resourcesDir = projectDir.resolve("buildSrc/src/main/resources/META-INF/gradle-plugins");
        Files.createDirectories(resourcesDir);

        // SpringBootExtension
        Path extensionFile = buildSrcDir.resolve("org/springframework/boot/gradle/dsl/SpringBootExtension.java");
        Files.writeString(
                extensionFile,
                """
                        package org.springframework.boot.gradle.dsl;
                        
                        import org.gradle.api.Project;
                        import org.gradle.api.tasks.TaskProvider;
                        
                        public class SpringBootExtension {
                            private final Project project;
                        
                            public SpringBootExtension(Project project) {
                                this.project = project;
                            }
                        
                            public TaskProvider<?> buildInfo() {
                                return project.getTasks().register("bootBuildInfo");
                            }
                        }
                        """,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        // SpringBootPlugin
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
                            }
                        }
                        """,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        // Plugin marker
        Path pluginMarker = resourcesDir.resolve("org.springframework.boot.properties");
        Files.writeString(
                pluginMarker,
                "implementation-class=org.springframework.boot.gradle.plugin.SpringBootPlugin",
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

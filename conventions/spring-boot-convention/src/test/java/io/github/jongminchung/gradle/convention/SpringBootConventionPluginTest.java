package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

class SpringBootConventionPluginTest {
    @Test
    void addsSpringBootDependenciesWhenSpringPluginApplied() {
        var project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(SpringBootPlugin.class);
        project.getPluginManager().apply(SpringBootConventionPlugin.class);

        evaluate(project);

        var implementation =
                project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
        assertThat(implementation.getDependencies())
                .anyMatch(dep -> "org.springframework.boot".equals(dep.getGroup())
                        && "spring-boot-starter".equals(dep.getName()));

        var testImplementation =
                project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME);
        var starterTest = (ModuleDependency) testImplementation.getDependencies().stream()
                .filter(dep -> "org.springframework.boot".equals(dep.getGroup())
                        && "spring-boot-starter-test".equals(dep.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(starterTest.getExcludeRules())
                .anyMatch(rule -> "org.junit.vintage".equals(rule.getGroup())
                        && "junit-vintage-engine".equals(rule.getModule()));

        var testRuntimeOnly =
                project.getConfigurations().getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME);
        assertThat(testRuntimeOnly.getDependencies())
                .anyMatch(dep -> "org.junit.platform".equals(dep.getGroup())
                        && "junit-platform-launcher".equals(dep.getName()));
    }

    @Test
    void bootBuildInfoDependsOnCompileJava() {
        var project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(SpringBootPlugin.class);
        project.getPluginManager().apply(SpringBootConventionPlugin.class);

        evaluate(project);

        var compileJava = project.getTasks().getByName("compileJava");
        var bootBuildInfo = project.getTasks().getByName("bootBuildInfo");

        assertThat(bootBuildInfo.getTaskDependencies().getDependencies(bootBuildInfo))
                .anyMatch(task -> task.getName().equals(compileJava.getName()));
        assertThat(bootBuildInfo.getMustRunAfter().getDependencies(bootBuildInfo))
                .anyMatch(task -> task.getName().equals(compileJava.getName()));
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}

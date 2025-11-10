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
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(SpringBootPlugin.class);
        project.getPluginManager().apply(SpringBootConventionPlugin.class);

        evaluate(project);

        Configuration implementation =
                project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
        assertThat(implementation.getDependencies())
                .anyMatch(dep -> "org.springframework.boot".equals(dep.getGroup())
                        && "spring-boot-starter".equals(dep.getName()));

        Configuration testImplementation =
                project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME);
        ModuleDependency starterTest = (ModuleDependency) testImplementation.getDependencies().stream()
                .filter(dep -> "org.springframework.boot".equals(dep.getGroup())
                        && "spring-boot-starter-test".equals(dep.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(starterTest.getExcludeRules())
                .anyMatch(rule -> "org.junit.vintage".equals(rule.getGroup())
                        && "junit-vintage-engine".equals(rule.getModule()));

        Configuration testRuntimeOnly =
                project.getConfigurations().getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME);
        assertThat(testRuntimeOnly.getDependencies())
                .anyMatch(dep -> "org.junit.platform".equals(dep.getGroup())
                        && "junit-platform-launcher".equals(dep.getName()));
    }

    @Test
    void bootBuildInfoDependsOnCompileJava() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(SpringBootPlugin.class);
        project.getPluginManager().apply(SpringBootConventionPlugin.class);

        evaluate(project);

        Task compileJava = project.getTasks().getByName("compileJava");
        Task bootBuildInfo = project.getTasks().getByName("bootBuildInfo");

        assertThat(bootBuildInfo.getDependsOn()).contains(compileJava);
        assertThat(bootBuildInfo.getMustRunAfter().getDependencies(bootBuildInfo)).contains(compileJava);
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}

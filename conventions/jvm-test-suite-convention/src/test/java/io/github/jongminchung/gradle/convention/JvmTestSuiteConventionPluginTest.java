package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testing.base.TestingExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class JvmTestSuiteConventionPluginTest {

    @Test
    void registersAdditionalTestSuites() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply("jvm-test-suite");
        project.getPluginManager().apply(JvmTestSuiteConventionPlugin.class);

        TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
        assertThat(testing.getSuites().findByName("functionalTest")).isNotNull();
        assertThat(testing.getSuites().findByName("integrationTest")).isNotNull();
        assertThat(testing.getSuites().findByName("performanceTest")).isNotNull();
    }

    @Test
    void functionalTestImplementationExtendsFromMainImplementation() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply("jvm-test-suite");
        project.getPluginManager().apply(JvmTestSuiteConventionPlugin.class);

        Configuration implementation =
                project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
        Configuration functionalTestImplementation =
                project.getConfigurations().getByName("functionalTestImplementation");

        assertThat(functionalTestImplementation.getExtendsFrom()).contains(implementation);
    }
}

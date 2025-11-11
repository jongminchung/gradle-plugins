package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin;
import org.junit.jupiter.api.Test;

class JacocoReportAggregationConventionPluginTest {

    @Test
    void wiresJacocoSubprojectsIntoAggregationConfiguration() {
        Project root = ProjectBuilder.builder().withName("root").build();
        Project library =
                ProjectBuilder.builder().withName("lib").withParent(root).build();

        library.getPluginManager().apply("jacoco");

        root.getPluginManager().apply(JacocoReportAggregationPlugin.class);
        root.getPluginManager().apply(JacocoReportAggregationConventionPlugin.class);

        evaluate(root);

        Dependency dependency = root
                .getConfigurations()
                .getByName(JacocoReportAggregationPlugin.JACOCO_AGGREGATION_CONFIGURATION_NAME)
                .getDependencies()
                .stream()
                .filter(ProjectDependency.class::isInstance)
                .findFirst()
                .orElseThrow();

        assertThat(((ProjectDependency) dependency).getPath()).isEqualTo(":lib");
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}

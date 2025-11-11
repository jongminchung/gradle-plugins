package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.junit.jupiter.api.Test;

class JacocoConventionPluginTest {

    @Test
    void registersExtensionWithDefaults() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(JacocoConventionPlugin.class);

        ExtraJacocoExtension extension = project.getExtensions().findByType(ExtraJacocoExtension.class);
        assertThat(extension).isNotNull();
        assertThat(extension.getEnabled().get()).isTrue();
    }

    @Test
    void configuresJacocoReportTasks() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply("jacoco");
        project.getPluginManager().apply(JacocoConventionPlugin.class);
        project.getTasks().register("customJacocoReport", JacocoReport.class, report -> {});

        evaluate(project);

        JacocoReport report = (JacocoReport) project.getTasks().getByName("customJacocoReport");
        assertThat(report.getReports().getXml().getRequired().get()).isTrue();
        assertThat(report.getReports().getHtml().getRequired().get()).isTrue();
        assertThat(report.getReports().getCsv().getRequired().get()).isFalse();
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}

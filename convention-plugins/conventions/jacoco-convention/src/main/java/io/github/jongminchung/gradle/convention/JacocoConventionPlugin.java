package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jspecify.annotations.NonNull;

public class JacocoConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();

        var extraJacocoExtension = extensions.create(ExtraJacocoExtension.EXTENSION_NAME, ExtraJacocoExtension.class);

        project.getPluginManager().withPlugin("jacoco", plugin -> {
            var reports = project.getTasks().withType(JacocoReport.class);

            reports.configureEach(task -> {
                task.onlyIf(t -> extraJacocoExtension.getEnabled().get());
                task.getReports().getXml().getRequired().set(true);
                task.getReports().getHtml().getRequired().set(true);
                task.getReports().getCsv().getRequired().set(false);
            });
        });
    }
}

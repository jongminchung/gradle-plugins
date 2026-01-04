package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jspecify.annotations.NonNull;

/**
 * Jacoco 설정을 프로젝트에 적용하는 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 기능을 수행합니다:
 * <ul>
 *   <li>{@code JacocoReport} 태스크의 리포트 형식을 XML 및 HTML로 설정합니다.</li>
 *   <li>{@link ExtraJacocoExtension}을 통해 Jacoco 리포트 생성 활성화 여부를 제어할 수 있도록 합니다.</li>
 * </ul>
 */
public class JacocoConventionPlugin implements Plugin<@NonNull Project> {

    /**
     * Jacoco 설정을 프로젝트에 적용합니다.
     *
     * <p>이 메서드는 {@code afterEvaluate}를 사용하지 않고 {@code onlyIf}를 활용하여
     * 실행 시점에 설정값이 적용되도록 합니다.
     *
     * @param project 적용 대상 프로젝트
     */
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

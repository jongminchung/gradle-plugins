package io.github.jongminchung.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootJar;

import io.github.jongminchung.gradle.convention.*;

import net.ltgt.gradle.errorprone.ErrorPronePlugin;

import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;

/**
 * Spring Boot 애플리케이션 개발에 필요한 모든 표준 컨벤션을 한 번에 적용하는 통합 플러그인입니다.
 *
 * <p>이 플러그인을 적용하면 다음과 같은 하위 플러그인과 컨벤션들이 자동으로 설정됩니다:
 * <ul>
 *   <li>자바 표준 설정 ({@link JavaConventionPlugin})</li>
 *   <li>Spring Boot 기본 설정 ({@link SpringBootConventionPlugin})</li>
 *   <li>테스트 환경 구축 ({@link JvmTestSuiteConventionPlugin})</li>
 *   <li>코드 품질 분석 ({@link ErrorProneConventionPlugin}, {@link SpotlessConventionPlugin})</li>
 *   <li>Lombok 자동 설정</li>
 * </ul>
 *
 * <p>개발자는 이 플러그인 하나만 추가함으로써 팀의 모든 표준 개발 환경을 즉시 구축할 수 있습니다.
 */
@SuppressWarnings("UnstableApiUsage")
public class SpringBootApplicationPlugin implements Plugin<@NonNull Project> {
    static final String SPRING_BOOT_JAR_NAME = "app.jar";

    /**
     * 모든 표준 컨벤션을 프로젝트에 통합 적용합니다.
     *
     * @param target 적용 대상 프로젝트
     */
    @Override
    public void apply(Project target) {
        var plugins = target.getPluginManager();

        plugins.apply(JavaPlugin.class);
        plugins.apply(JavaConventionPlugin.class);

        plugins.apply(SpringBootPlugin.class);
        plugins.apply(SpringBootConventionPlugin.class);

        target.getTasks().named("bootJar", BootJar.class, bootJar -> bootJar.getArchiveFileName()
                .set(SPRING_BOOT_JAR_NAME));

        plugins.apply(DependencyManagementPlugin.class);

        plugins.apply(JacocoPlugin.class);
        plugins.apply(JacocoConventionPlugin.class);

        plugins.apply(JacocoReportAggregationPlugin.class);

        plugins.apply(JvmTestSuiteConventionPlugin.class);

        plugins.apply(ErrorPronePlugin.class);
        plugins.apply(ErrorProneConventionPlugin.class);
        plugins.apply(SpotlessConventionPlugin.class);

        plugins.apply(LombokPlugin.class);
    }
}

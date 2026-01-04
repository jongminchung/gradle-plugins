package io.github.jongminchung.gradle.convention;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import com.diffplug.spotless.kotlin.KtLintStep;

/**
 * 코드 포맷팅 도구인 Spotless 설정을 프로젝트에 적용하는 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 기능을 수행합니다:
 * <ul>
 *   <li>Java: Palantir Java Format을 적용하고, Javadoc 포맷팅, 미사용 임포트 제거, 임포트 순서 등을 설정합니다.</li>
 *   <li>Kotlin: ktlint를 적용하고 사용자 정의 규칙 세트를 구성합니다.</li>
 *   <li>Kotlin Gradle Script: ktlint를 적용하여 빌드 스크립트의 스타일을 관리합니다.</li>
 * </ul>
 */
public class SpotlessConventionPlugin implements Plugin<@NonNull Project> {
    private static final String PALANTIR_JAVA_FORMAT_VERSION = "2.82.0";
    private static final String KTLINT_VERSION = KtLintStep.defaultVersion();
    private static final String KTLINT_RULE_ENGINE_CORE_COORDINATE =
            "com.pinterest.ktlint:ktlint-rule-engine-core:" + KTLINT_VERSION;
    private static final String KOTLIN_VERSION = "2.2.0";
    private static final List<String> KTLINT_SUPPLEMENTAL_COORDINATES = List.of(
            KTLINT_RULE_ENGINE_CORE_COORDINATE,
            "org.jetbrains.kotlin:kotlin-compiler-embeddable:" + KOTLIN_VERSION,
            "org.jetbrains.kotlin:kotlin-stdlib:" + KOTLIN_VERSION);

    /**
     * Spotless 설정을 프로젝트에 적용합니다.
     *
     * @param target 적용 대상 프로젝트
     */
    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(SpotlessPlugin.class);

        target.getExtensions().configure(SpotlessExtension.class, spotless -> {
            spotless.java(java -> {
                java.palantirJavaFormat(PALANTIR_JAVA_FORMAT_VERSION).formatJavadoc(true);

                java.formatAnnotations();
                java.removeUnusedImports();
                java.trimTrailingWhitespace();

                java.importOrder("java", "jakarta", "org", "com", "net", "io", "lombok");

                java.targetExclude("**/build/**");
            });

            spotless.kotlin(kotlin -> {
                try {
                    kotlin.ktlint().customRuleSets(KTLINT_SUPPLEMENTAL_COORDINATES);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to configure ktlint for Kotlin", e);
                }
                kotlin.trimTrailingWhitespace();
            });

            spotless.kotlinGradle(kotlinGradle -> {
                try {
                    kotlinGradle.ktlint().customRuleSets(KTLINT_SUPPLEMENTAL_COORDINATES);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to configure ktlint for Kotlin Gradle scripts", e);
                }
                kotlinGradle.trimTrailingWhitespace();
            });
        });
    }
}

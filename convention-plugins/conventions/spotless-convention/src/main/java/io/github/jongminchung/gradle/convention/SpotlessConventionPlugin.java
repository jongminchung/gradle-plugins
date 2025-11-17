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

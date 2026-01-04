package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jspecify.annotations.NonNull;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import net.ltgt.gradle.nullaway.NullAwayExtension;
import net.ltgt.gradle.nullaway.NullAwayOptions;
import net.ltgt.gradle.nullaway.NullAwayPlugin;

import java.util.Collections;

/**
 * ErrorProne 및 NullAway 설정을 프로젝트에 적용하는 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 기능을 수행합니다:
 * <ul>
 *   <li>{@code ErrorProne} 및 {@code NullAway} 플러그인을 적용합니다.</li>
 *   <li>{@code JavaCompile} 태스크에 ErrorProne 옵션을 설정하고, 특정 검사 항목의 심각도를 {@code ERROR}로 상향합니다.</li>
 *   <li>NullAway의 점진적 도입을 위해 {@code @NullMarked} 어노테이션 기반 검사 설정을 지원합니다.</li>
 *   <li>{@code JSpecify} 의존성을 자동으로 추가합니다.</li>
 * </ul>
 */
public class ErrorProneConventionPlugin implements Plugin<@NonNull Project> {
    static final String JSPECIFY = "org.jspecify:jspecify:1.0.0";
    static final String NULLAWAY_DEPENDENCY = "com.uber.nullaway:nullaway:";
    static final String ERROR_PRONE_DEPENDENCY = "com.google.errorprone:error_prone_core:";

    /**
     * ErrorProne 및 NullAway 설정을 프로젝트에 적용합니다.
     *
     * <p>이 메서드는 {@code afterEvaluate}를 사용하지 않고 {@code withPlugin} 및 Lazy Configuration API를 사용하여
     * 플러그인이 적용되는 시점에 설정을 연결합니다.
     *
     * @param target 적용 대상 프로젝트
     */
    @Override
    public void apply(Project target) {
        var extraErrorProne =
                target.getExtensions().create(ExtraErrorProneExtension.EXTENSION_NAME, ExtraErrorProneExtension.class);

        target.getPlugins().apply(ErrorPronePlugin.class);
        target.getPlugins().apply(NullAwayPlugin.class);

        target.getPluginManager().withPlugin("java", unused -> {
            target.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, JSPECIFY);

            var nullawayDependency =
                    NULLAWAY_DEPENDENCY + extraErrorProne.getNullAwayVersion().get();
            target.getDependencies().add(ErrorPronePlugin.CONFIGURATION_NAME, nullawayDependency);

            var errorProneDependency = ERROR_PRONE_DEPENDENCY
                    + extraErrorProne.getErrorProneVersion().get();
            target.getDependencies().add(ErrorPronePlugin.CONFIGURATION_NAME, errorProneDependency);

            target.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                var compileOptions = javaCompile.getOptions();
                compileOptions.setFork(true);
                compileOptions.getForkOptions().setMemoryMaximumSize("4g");

                ExtensionAware opts = (ExtensionAware) javaCompile.getOptions();
                ErrorProneOptions errorProneOptions = opts.getExtensions().findByType(ErrorProneOptions.class);
                if (errorProneOptions == null) return;

                errorProneOptions.getExcludedPaths().set(".*/(build|out|\\.gradle)/.*");

                // errorProneOptions.getAllDisabledChecksAsWarnings().set(true); (compileJava에서 오래 점유함..)
                errorProneOptions.check("UnicodeInCode", CheckSeverity.WARN);
                configWarnOptionsToErrorOptions(errorProneOptions);

                ExtensionAware errorproneExt = (ExtensionAware) errorProneOptions;
                var nullaway = errorproneExt.getExtensions().findByType(NullAwayOptions.class);
                if (nullaway != null) {
                    if (extraErrorProne.getUseNullMarked().get()) {
                        errorProneOptions.option("NullAway:OnlyNullMarked", "true"); // Enable nullness checks only in null-marked code
                        errorProneOptions.option("NullAway:JSpecifyMode", "true"); // https://github.com/uber/NullAway/wiki/JSpecify-Support
                    }
                    nullaway.error(); // bump checks from warnings (default) to errors
                }
            });

            target.getPlugins().withType(NullAwayPlugin.class, unused2 -> {
                var nullawayExt = target.getExtensions().getByType(NullAwayExtension.class);
                nullawayExt.getOnlyNullMarked().set(extraErrorProne.getUseNullMarked());

                nullawayExt.getAnnotatedPackages().addAll(target.getProviders().provider(() -> {
                    if (extraErrorProne.getUseNullMarked().get()) {
                        return Collections.emptyList();
                    }
                    var group = String.valueOf(target.getGroup());
                    return group.isBlank() ? Collections.emptyList() : Collections.singletonList(group);
                }));
            });
        });
    }

    private static void configWarnOptionsToErrorOptions(ErrorProneOptions errorProneOptions) {
        errorProneOptions.check("UnusedMethod", CheckSeverity.ERROR);
        errorProneOptions.check("UnusedVariable", CheckSeverity.ERROR);

        errorProneOptions.check("ReferenceEquality", CheckSeverity.ERROR);
        errorProneOptions.check("BigDecimalEquals", CheckSeverity.ERROR);

        errorProneOptions.check("MissingOverride", CheckSeverity.ERROR);
    }
}

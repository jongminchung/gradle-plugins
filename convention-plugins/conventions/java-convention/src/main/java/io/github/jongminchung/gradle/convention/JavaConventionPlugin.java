package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jspecify.annotations.NonNull;

/**
 * Java 프로젝트의 공통 컨벤션을 설정하는 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 설정을 수행합니다:
 * <ul>
 *   <li>{@link JavaPluginExtension}을 통해 Javadoc 및 Sources Jar 생성을 설정합니다.</li>
 *   <li>Javadoc 태스크의 옵션을 조정하여 lint 에러를 방지합니다.</li>
 * </ul>
 */
public class JavaConventionPlugin implements Plugin<@NonNull Project> {

    /**
     * Java 컨벤션을 프로젝트에 적용합니다.
     *
     * <p>이 메서드는 {@code afterEvaluate} 사용을 지양하고 Gradle의 Lazy Configuration API를 활용합니다.
     * {@code withJavadocJar()}와 {@code withSourcesJar()}는 호출 즉시 내부적으로 Task를 생성하고
     * Artifact로 등록하는 부작용(Side-effect)이 있어 지연 평가가 까다롭습니다.
     *
     * <p>해결 전략:
     * <ol>
     *   <li>설정 단계: {@code getOrElse(true)}를 사용하여 값이 확정되지 않았더라도 기본적으로 Task를 생성하도록 유도합니다.
     *       이는 {@code maven-publish}와 같은 다른 플러그인이 프로젝트 평가 중에 Artifact 정보를 수집할 수 있도록 하기 위함입니다.</li>
     *   <li>실행 단계: {@code onlyIf}를 통해 실제 Task의 실행 여부를 실행 시점(Execution Phase)에 결정합니다.
     *       이때는 사용자의 설정값이 확정되어 있으므로 {@code Property.get()}을 안전하게 호출할 수 있습니다.</li>
     * </ol>
     *
     * @param target 적용 대상 프로젝트
     */
    @Override
    public void apply(Project target) {
        var extraJava = target.getExtensions().create(ExtraJavaExtension.EXTENSION_NAME, ExtraJavaExtension.class);

        target.getPluginManager().withPlugin("java", applied -> {
            target.getExtensions().configure(JavaPluginExtension.class, javaExt -> {
                if (extraJava.getEnabled().getOrElse(true)) {
                    if (extraJava.getWithJavadocJar().getOrElse(true)) {
                        javaExt.withJavadocJar();
                    }
                    if (extraJava.getWithSourcesJar().getOrElse(true)) {
                        javaExt.withSourcesJar();
                    }
                }
            });

            target.getTasks().withType(Javadoc.class).configureEach(javadoc -> {
                javadoc.onlyIf(t -> extraJava.getEnabled().get() && extraJava.getWithJavadocJar().getOrElse(true));
                var opts = (CoreJavadocOptions) javadoc.getOptions();
                opts.addStringOption("Xdoclint:none", "-quiet");
            });
        });
    }
}

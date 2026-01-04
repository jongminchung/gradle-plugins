package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jspecify.annotations.NonNull;

public class JavaConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        var extraJava = target.getExtensions().create(ExtraJavaExtension.EXTENSION_NAME, ExtraJavaExtension.class);

        target.getPluginManager().withPlugin("java", applied -> {
            target.getExtensions().configure(JavaPluginExtension.class, javaExt -> {
                // withJavadocJar()와 withSourcesJar()는 즉시 호출되어야 하며, 내부적으로 Task를 생성합니다.
                // Property의 값을 직접적으로 사용할 수 없으므로, onlyIf 등을 활용하여 지연시키거나
                // 적용 시점에 결정해야 합니다. 여기서는 apply 시점에 결정하거나 Provider를 활용하기 어렵다면
                // 조건부 설정을 유지하되 afterEvaluate를 피하는 방법을 찾습니다.

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
                CoreJavadocOptions opts = (CoreJavadocOptions) javadoc.getOptions();
                opts.addStringOption("Xdoclint:none", "-quiet");
            });
        });
    }
}

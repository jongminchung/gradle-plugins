package io.github.jongmin_chung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jspecify.annotations.NonNull;

public class JavaConventionPlugin implements Plugin<@NonNull Project> {

    private static final String JAVA_PLUGIN_NAME = "java";

    @Override
    public void apply(Project target) {
        var extraJava = target.getExtensions().create(ExtraJavaExtension.EXTENSION_NAME, ExtraJavaExtension.class);

        target.afterEvaluate(project -> {
            if (!extraJava.enabled.get()) {
                return;
            }

            var pluginManager = project.getPluginManager();

            pluginManager.withPlugin(JAVA_PLUGIN_NAME, applied -> project.getExtensions()
                    .configure(JavaPluginExtension.class, javaExt -> {
                        if (extraJava.withJavadocJar.getOrElse(true)) {
                            javaExt.withJavadocJar();

                            project.getTasks().withType(Javadoc.class).configureEach(javadoc -> {
                                CoreJavadocOptions opts = (CoreJavadocOptions) javadoc.getOptions();
                                opts.addStringOption("Xdoclint:none", "-quiet");
                            });
                        }

                        if (extraJava.withSourcesJar.getOrElse(true)) {
                            javaExt.withSourcesJar();
                        }
                    }));
        });
    }
}

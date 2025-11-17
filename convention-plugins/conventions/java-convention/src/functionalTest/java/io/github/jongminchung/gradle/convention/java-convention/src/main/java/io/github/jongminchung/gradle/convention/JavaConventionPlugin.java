import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.jspecify.annotations.NonNull;

public class JavaConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        var extraJava = target.getExtensions()
                .create(
                        io.github.jongminchung.gradle.convention.java.ExtraJavaExtension.EXTENSION_NAME,
                        ExtraJavaExtension.class);

        target.afterEvaluate(project -> {
            if (!extraJava.getEnabled().get()) {
                return;
            }

            var pluginManager = project.getPluginManager();

            pluginManager.withPlugin(
                    "java", applied -> project.getExtensions().configure(JavaPluginExtension.class, javaExt -> {
                        if (extraJava.getWithJavadocJar().getOrElse(true)) {
                            javaExt.withJavadocJar();

                            project.getTasks().withType(Javadoc.class).configureEach(javadoc -> {
                                CoreJavadocOptions opts = (CoreJavadocOptions) javadoc.getOptions();
                                opts.addStringOption("Xdoclint:none", "-quiet");
                            });
                        }

                        if (extraJava.getWithSourcesJar().getOrElse(true)) {
                            javaExt.withSourcesJar();
                        }
                    }));
        });
    }
}

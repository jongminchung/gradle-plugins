package io.github.jongminchung.gradle.plugin;

import com.github.gradle.node.NodeExtension;
import com.github.gradle.node.NodePlugin;
import com.github.gradle.node.npm.task.NpxTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

import java.util.HashMap;
import java.util.List;

public class OpenApiSpringPlugin implements Plugin<@NonNull Project> {
    private static final String GROUP_NAME = "openapi";

    private static final String LINT_TASK_NAME = "lintOpenApiSpec";
    private static final String BUNDLE_TASK_NAME = "bundleOpenApiSpec";
    private static final String GENERATE_TASK_NAME = "generateOpenApi";

    private static final String GENERATED_OPENAPI_PATH = "generated/openapi";

    @Override
    public void apply(@NonNull Project target) {
        if (!target.getPlugins().hasPlugin(JavaPlugin.class)) {
            target.getPlugins().apply(JavaPlugin.class);
        }
        if (!target.getPlugins().hasPlugin(NodePlugin.class)) {
            target.getPlugins().apply(NodePlugin.class);
        }
        if (!target.getPlugins().hasPlugin(OpenApiGeneratorPlugin.class)) {
            target.getPlugins().apply(OpenApiGeneratorPlugin.class);
        }

        target.getExtensions().configure(NodeExtension.class, node -> {
            node.getDownload().set(true);
            node.getVersion().set("24.11.1");
        });

        var extension = target.getExtensions().create("openapiSpring", OpenApiSpringExtension.class);

        extension.getInputFile().convention(
                target.getLayout()
                        .getProjectDirectory()
                        .dir(GROUP_NAME)
                        .file("openapi-spec.yml")
        );
        extension.getOutputFile().convention(
                target.getLayout()
                        .getBuildDirectory()
                        .file("openapi/openapi-spec.yml")
        );

        registerLintTask(target, extension);
        registerBundleTask(target, extension);
        registerGenerateTask(target, extension);
    }

    private void registerGenerateTask(Project project, OpenApiSpringExtension extension) {
        var output = project.getLayout().getBuildDirectory().dir(GENERATED_OPENAPI_PATH);

        var generateTaskProvider = project.getTasks().register(GENERATE_TASK_NAME, GenerateTask.class, task -> {
            task.setGroup(GROUP_NAME);
            task.setDescription("Generate Spring server stubs from OpenAPI specification.");

            task.dependsOn(project.getTasks().named(BUNDLE_TASK_NAME));
            task.getInputs().file(extension.getOutputFile());
            task.getOutputs().dir(output.map(it -> it.getAsFile().getPath()));

            task.getInputSpec().set(extension.getOutputFile().map(f -> f.getAsFile().getAbsolutePath()));
            task.getOutputDir().set(output.map(dir -> dir.getAsFile().getAbsolutePath()));

            task.getGeneratorName().set("spring");
            task.getLibrary().set("spring-boot");

            HashMap<String, String> props = new HashMap<>();
            props.put("artifactId", project.getName());
            props.put("artifactVersion", project.getVersion().toString());
            props.put("groupId", project.getGroup().toString());

            props.put("interfaceOnly", "true"); // 인터페이스만 생성
            props.put("useBeanValidation", "true"); // JSR303, JSR380 bean validation annotation

            props.put("dateLibrary", "java8");
            props.put("openApiNullable", "false");

            props.put("useTags", "true");
            props.put("documentationProvider", "none");
            props.put("hideGenerationTimestamp", "true");
            props.put("useSpringBoot3", "true");

            task.getAdditionalProperties().set(props);
        });

        project.getExtensions().configure(SourceSetContainer.class, sourceSets -> sourceSets.getByName("main").getJava().srcDirs(output));

        project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class)
                .configure(task -> task.dependsOn(generateTaskProvider));
    }

    private void registerLintTask(Project project, OpenApiSpringExtension extension) {
        project.getTasks().register(LINT_TASK_NAME, NpxTask.class, task -> {
            task.setGroup(GROUP_NAME);
            task.setDescription("Lint OpenAPI specification using Redocly CLI.");
            task.getInputs().dir(extension.getInputFile().map(f ->
                    f.getAsFile().getParentFile()
            ));

            task.getCommand().set("npx");
            task.getArgs().set(
                    extension.getInputFile().map(input ->
                            List.of(
                                    "@redocly/cli",
                                    "lint",
                                    input.getAsFile().getAbsolutePath()
                            ))
            );
        });
    }

    private static void registerBundleTask(
            Project project,
            OpenApiSpringExtension extension
    ) {
        project.getTasks().register(BUNDLE_TASK_NAME, NpxTask.class, task -> {
            task.dependsOn(project.getTasks().named(LINT_TASK_NAME));

            task.setGroup(GROUP_NAME);
            task.setDescription("Bundle OpenAPI specification using Redocly CLI.");
            task.getInputs().file(extension.getInputFile());
            task.getOutputs().file(extension.getOutputFile());

            task.getCommand().set("npx");
            Provider<@NotNull List<String>> argsProvider =
                    project.getProviders().provider(() -> {
                        var in = extension.getInputFile().get().getAsFile();
                        var out = extension.getOutputFile().get().getAsFile();

                        return List.of(
                                "@redocly/cli",
                                "bundle",
                                in.getAbsolutePath(),
                                "--output",
                                out.getAbsolutePath()
                        );
                    });

            task.getArgs().set(argsProvider);
        });
    }
}

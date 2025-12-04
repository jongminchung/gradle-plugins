package io.github.jongminchung.gradle.plugin;

import com.github.gradle.node.NodeExtension;
import com.github.gradle.node.NodePlugin;
import com.github.gradle.node.npm.task.NpxTask;
import org.gradle.api.GradleException;
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

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class OpenApiSpringGeneratorPlugin implements Plugin<@NonNull Project> {
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
            node.getVersion().set("24.11.0");
            node.getWorkDir().set(target.getLayout().getProjectDirectory().dir(".gradle/nodejs"));
            node.getNpmWorkDir().set(target.getLayout().getProjectDirectory().dir(".gradle/npm"));
        });

        var extension = target.getExtensions().create("openapiSpringGenerator", OpenApiSpringGeneratorExtension.class);

        extension.getInputFile().convention(
                target.getLayout()
                        .getProjectDirectory()
                        .dir(GROUP_NAME)
                        .file("openapi-spec.yaml")
        );
        extension.getOutputFile().convention(
                target.getLayout()
                        .getBuildDirectory()
                        .file("openapi/openapi-spec.yaml")
        );

        registerLintTask(target, extension);
        registerBundleTask(target, extension);
        registerGenerateTask(target, extension);
//        hideNodeUtilityTaskGroups(target);
    }

//    private void hideNodeUtilityTaskGroups(Project project) {
//        var hiddenGroups = List.of("node", "npm", "pnpm", "yarn");
//        project.getTasks().configureEach(task -> {
//            var group = task.getGroup();
//            if (group != null && hiddenGroups.contains(group)) {
//                task.setGroup(null); // keep tasks functional but hide from Gradle task listings
//            }
//        });
//    }

    private void registerGenerateTask(Project project, OpenApiSpringGeneratorExtension extension) {
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

            String basePackage = project.getGroup() + ".openapi";
            props.put("basePackage", basePackage);
            props.put("apiPackage", basePackage);
            props.put("modelPackage", basePackage + ".dto");
            props.put("configPackage", basePackage + ".config");
            props.put("invokerPackage", basePackage);

            props.put("interfaceOnly", "true"); // 인터페이스만 생성
            props.put("skipDefaultInterface", "true"); // default interface 기본적으로 생성되는 것을 막음
            props.put("useBeanValidation", "true"); // JSR303, JSR380 bean validation annotation

            props.put("dateLibrary", "java8");
            props.put("openApiNullable", "false");

            props.put("useTags", "true");
            props.put("documentationProvider", "none");
            props.put("hideGenerationTimestamp", "true");
            props.put("useSpringBoot3", "true");
            props.put("useResponseEntity", "true");

            task.getAdditionalProperties().set(props);
        });

        project.getExtensions().configure(SourceSetContainer.class, sourceSets ->
                sourceSets.getByName("main").getJava().srcDirs(output.map(dir -> dir.dir("src/main/java"))));

        project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class)
                .configure(task -> task.dependsOn(generateTaskProvider));
    }

    private void registerLintTask(Project project, OpenApiSpringGeneratorExtension extension) {
        project.getTasks().register(LINT_TASK_NAME, NpxTask.class, task -> {
            task.setGroup(GROUP_NAME);
            task.setDescription("Lint OpenAPI specification using Redocly CLI.");

            var inputDirProvider = extension.getInputFile().map(f -> f.getAsFile().getParentFile());
            task.getInputs().files(inputDirProvider.map(dir ->
                    project.fileTree(dir, tree -> tree.include("**/*.yaml", "**/*.yml", "**/*.json"))
            ));

            var markerFileProvider = project.getLayout().getBuildDirectory()
                    .dir(GROUP_NAME)
                    .map(dir -> dir.file("lint-success.marker"));
            task.getOutputs().file(markerFileProvider);

            task.getCommand().set("npx");
            task.getArgs().set(
                    extension.getInputFile().map(input ->
                            List.of(
                                    "@redocly/cli",
                                    "lint",
                                    input.getAsFile().getAbsolutePath()
                            ))
            );

            task.doLast(t -> {
                try {
                    var message = "Lint Success at: " + LocalDateTime.now(ZoneId.systemDefault());
                    Files.writeString(markerFileProvider.get().getAsFile().toPath(), message);
                } catch (Exception e) {
                    throw new GradleException("Failed to update lint marker file", e);
                }
            });
        });
    }

    private void registerBundleTask(
            Project project,
            OpenApiSpringGeneratorExtension extension
    ) {
        project.getTasks().register(BUNDLE_TASK_NAME, NpxTask.class, task -> {
            task.dependsOn(project.getTasks().named(LINT_TASK_NAME));

            task.setGroup(GROUP_NAME);
            task.setDescription("Bundle OpenAPI specification using Redocly CLI.");
            var inputDirProvider = extension.getInputFile().map(f -> f.getAsFile().getParentFile());
            task.getInputs().files(inputDirProvider.map(dir ->
                    project.fileTree(dir, tree -> tree.include("**/*.yaml", "**/*.yml", "**/*.json"))
            ));
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

            task.doLast(t -> {
                var outputDir = extension.getOutputFile().get().getAsFile().getParentFile();
                var outputFile = outputDir.toPath().resolve("index.html");
                try (var input = Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("redoc.html"),
                        "redoc.html resource not found in plugin jar"
                )) {
                    createDirectories(outputDir.toPath());
                    copy(input, outputFile, REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new GradleException("Failed to copy API docs", e);
                }
            });
        });
    }
}

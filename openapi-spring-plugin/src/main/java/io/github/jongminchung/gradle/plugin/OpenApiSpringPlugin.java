package io.github.jongminchung.gradle.plugin;

import com.github.gradle.node.NodeExtension;
import com.github.gradle.node.NodePlugin;
import com.github.gradle.node.npm.task.NpxTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class OpenApiSpringPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(@NonNull Project target) {
        target.getPlugins().apply(NodePlugin.class);
        target.getExtensions().configure(NodeExtension.class, node -> {
            node.getDownload().set(true);
            node.getVersion().set("24.11.1");
            node.getWorkDir().set(target.getLayout().getProjectDirectory().dir(".gradle/nodejs"));
        });

        var extension = target.getExtensions().create("openapiSpring", OpenApiSpringExtension.class);

        extension.getInputFile().convention(
                target.getLayout()
                        .getProjectDirectory()
                        .dir("openapi")
                        .file("openapi-spec.yml")
        );
        extension.getOutputFile().convention(
                target.getLayout()
                        .getBuildDirectory()
                        .file("openapi/openapi-spec.yml")
        );

        // 번들링 태스크 등록
        registerBundleTask(target, extension);
    }

    private static void registerBundleTask(
            @NonNull Project project,
            @NonNull OpenApiSpringExtension extension
    ) {
        project.getTasks().register("bundle", NpxTask.class, task -> {
            task.setGroup("openapi");
            task.setDescription("Bundle OpenAPI specification using Redocly CLI.");
            task.getInputs().file(extension.getInputFile());
            task.getOutputs().file(extension.getOutputFile());

            task.dependsOn("npmInstall");

            task.getCommand().set("npx");
            task.getArgs().set(
                    List.of(
                            "@redocly/cli",
                            "bundle",
                            extension.getInputFile().getAsFile().get().getAbsolutePath(),
                            "--output",
                            extension.getOutputFile().getAsFile().get().getAbsolutePath(
                            )
                    )
            );
        });
    }
}

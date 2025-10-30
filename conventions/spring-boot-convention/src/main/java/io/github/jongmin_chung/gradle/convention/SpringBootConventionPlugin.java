package io.github.jongmin_chung.gradle.convention;

import static org.gradle.api.internal.tasks.JvmConstants.COMPILE_JAVA_TASK_NAME;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.gradle.dsl.SpringBootExtension;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

public class SpringBootConventionPlugin implements Plugin<@NonNull Project> {
    private static final String BOOT_BUILD_INFO_TASK_NAME = "bootBuildInfo";
    private static final String SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter";
    private static final String SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test";
    private static final String JUNIT_VINTAGE_GROUP = "org.junit.vintage";
    private static final String JUNIT_VINTAGE_ENGINE = "junit-vintage-engine";
    private static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";

    @Override
    public void apply(Project target) {
        target.afterEvaluate(project -> {
            if (!project.getPlugins().hasPlugin(SpringBootPlugin.class)) {
                return;
            }

            var deps = project.getDependencies();
            deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, SPRING_BOOT_STARTER);

            var sbTest = (ModuleDependency) deps.create(SPRING_BOOT_STARTER_TEST);
            sbTest.exclude(Map.of("group", JUNIT_VINTAGE_GROUP, "module", JUNIT_VINTAGE_ENGINE));
            deps.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, sbTest);
            deps.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, JUNIT_PLATFORM_LAUNCHER);

            var tasks = project.getTasks();
            if (tasks.findByName(BOOT_BUILD_INFO_TASK_NAME) == null) {
                project.getExtensions().configure(SpringBootExtension.class, SpringBootExtension::buildInfo);
            }
            var compileJava = tasks.named(COMPILE_JAVA_TASK_NAME);
            var bootBuildInfo = tasks.named(BOOT_BUILD_INFO_TASK_NAME);

            bootBuildInfo.configure(tBoot -> {
                tBoot.mustRunAfter(compileJava);
                tBoot.dependsOn(compileJava);
            });
        });
    }
}

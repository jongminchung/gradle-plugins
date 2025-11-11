package io.github.jongminchung.gradle.convention;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jspecify.annotations.NonNull;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;

public class SpringBootConventionPlugin implements Plugin<@NonNull Project> {
    private static final String SPRING_BOOT_PLUGIN_ID = "org.springframework.boot";
    private static final String BOOT_BUILD_INFO_TASK_NAME = "bootBuildInfo";
    private static final String SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter";
    private static final String SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test";
    private static final String JUNIT_VINTAGE_GROUP = "org.junit.vintage";
    private static final String JUNIT_VINTAGE_ENGINE = "junit-vintage-engine";
    private static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";
    private static final String SPRING_BOOT_EXTENSION_NAME = "springBoot";

    @Override
    public void apply(Project target) {
        target.getPlugins().withId(SPRING_BOOT_PLUGIN_ID, plugin -> {
            configureDependencies(target);
            configureBootBuildInfo(target);
        });
    }

    private void configureDependencies(Project project) {
        var deps = project.getDependencies();
        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, SPRING_BOOT_STARTER);

        var sbTest = (ModuleDependency) deps.create(SPRING_BOOT_STARTER_TEST);
        sbTest.exclude(Map.of("group", JUNIT_VINTAGE_GROUP, "module", JUNIT_VINTAGE_ENGINE));
        deps.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, sbTest);
        deps.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, JUNIT_PLATFORM_LAUNCHER);
    }

    private void configureBootBuildInfo(Project project) {
        project.afterEvaluate(p -> {
            var tasks = p.getTasks();
            if (tasks.findByName(BOOT_BUILD_INFO_TASK_NAME) == null) {
                // Use reflection to avoid compile-time dependency on SpringBootExtension
                var springBootExt = p.getExtensions().findByName(SPRING_BOOT_EXTENSION_NAME);
                if (springBootExt != null) {
                    try {
                        var buildInfoMethod = springBootExt.getClass().getMethod("buildInfo");
                        buildInfoMethod.invoke(springBootExt);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to configure bootBuildInfo", e);
                    }
                }
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

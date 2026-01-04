package io.github.jongminchung.gradle.convention;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jspecify.annotations.NonNull;

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
        // Use withType(Object.class) or search for extension manually inside a lambda
        // Spring Boot plugin ensures the extension exists before this is called
        project.getExtensions().configure(SPRING_BOOT_EXTENSION_NAME, springBootExt -> {
            try {
                var buildInfoMethod = springBootExt.getClass().getMethod("buildInfo");
                buildInfoMethod.invoke(springBootExt);
            } catch (Exception e) {
                throw new RuntimeException("Failed to call buildInfo() on springBoot extension", e);
            }
        });

        project.getTasks().matching(t -> t.getName().equals(BOOT_BUILD_INFO_TASK_NAME)).configureEach(tBoot -> {
            var compileJava = project.getTasks().named(COMPILE_JAVA_TASK_NAME);
            tBoot.mustRunAfter(compileJava);
            tBoot.dependsOn(compileJava);
        });
    }
}

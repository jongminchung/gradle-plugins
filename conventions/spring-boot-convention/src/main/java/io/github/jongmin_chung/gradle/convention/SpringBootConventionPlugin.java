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
    private static final String SPRING_BOOT_PLUGIN_ID = "org.springframework.boot";
    private static final String BOOT_BUILD_INFO_TASK_NAME = "bootBuildInfo";
    private static final String ERRORPRONE_CONFIGURATION_NAME = "errorprone";
    private static final String SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter";
    private static final String SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test";
    private static final String JUNIT_VINTAGE_GROUP = "org.junit.vintage";
    private static final String JUNIT_VINTAGE_ENGINE = "junit-vintage-engine";
    private static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";
    private static final String JSPECIFY = "org.jspecify:jspecify";
    private static final String NULLAWAY = "com.uber.nullaway:nullaway";
    private static final String ERROR_PRONE_CORE = "com.google.errorprone:error_prone_core";
    private static final String CONFIGURED_FLAG =
            "io.github.jongmin_chung.gradle.convention.SpringBootConventionPlugin.CONFIGURED";

    @Override
    public void apply(Project project) {
        var pluginManager = project.getPluginManager();
        Runnable configure = () -> configureProject(project);

        project.getPlugins().withType(SpringBootPlugin.class).all(__ -> configure.run());
        pluginManager.withPlugin(SPRING_BOOT_PLUGIN_ID, __ -> configure.run());

        if (project.getExtensions().findByType(SpringBootExtension.class) != null) {
            configure.run();
        }
    }

    private void configureProject(Project project) {
        var extraProps = project.getExtensions().getExtraProperties();
        if (extraProps.has(CONFIGURED_FLAG)) {
            return;
        }
        extraProps.set(CONFIGURED_FLAG, true);

        // 1) bootBuildInfo 켜기
        project.getExtensions().configure(SpringBootExtension.class, SpringBootExtension::buildInfo);

        // 2) 의존성 추가
        var deps = project.getDependencies();
        var confs = project.getConfigurations();

        deps.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, SPRING_BOOT_STARTER);

        var errorprone = confs.findByName(ERRORPRONE_CONFIGURATION_NAME);
        if (errorprone != null) {
            errorprone.getDependencies().add(deps.create(NULLAWAY));
            errorprone.getDependencies().add(deps.create(ERROR_PRONE_CORE));
        }

        var sbTest = (ModuleDependency) deps.create(SPRING_BOOT_STARTER_TEST);
        sbTest.exclude(Map.of("group", JUNIT_VINTAGE_GROUP, "module", JUNIT_VINTAGE_ENGINE));
        deps.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, sbTest);
        deps.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, JUNIT_PLATFORM_LAUNCHER);

        // 3) 태스크 순서 (configuration avoidance)
        var tasks = project.getTasks();
        tasks.named(
                COMPILE_JAVA_TASK_NAME,
                tCompile -> tasks.named(BOOT_BUILD_INFO_TASK_NAME, tBoot -> {
                    tBoot.mustRunAfter(tCompile);
                    tBoot.dependsOn(tCompile);
                }));
    }
}

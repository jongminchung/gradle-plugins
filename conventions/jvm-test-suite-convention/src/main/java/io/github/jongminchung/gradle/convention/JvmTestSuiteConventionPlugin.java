package io.github.jongminchung.gradle.convention;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.testing.base.TestingExtension;
import org.jspecify.annotations.NonNull;

public class JvmTestSuiteConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(JavaPlugin.class);
        target.getPluginManager().apply("jvm-test-suite");

        var pluginManager = target.getPluginManager();
        pluginManager.withPlugin("java", javaPlugin ->
                pluginManager.withPlugin("jvm-test-suite", ignored -> configureTestingExtension(target)));
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void configureTestingExtension(Project target) {
        var testing = target.getExtensions().findByType(TestingExtension.class);
        if (testing == null) {
            return;
        }

        testing.getSuites().configureEach(suite -> {
            if (suite instanceof JvmTestSuite jvmTestSuite) {
                jvmTestSuite.useJUnitJupiter();
            }
        });

        testing.getSuites().register("functionalTest", JvmTestSuite.class, suite -> {
            String implConfName = suite.getSources().getImplementationConfigurationName();
            target.getConfigurations()
                    .named(implConfName)
                    .configure(conf -> conf.extendsFrom(
                            target.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)));

            suite.getTargets().all(t -> t.getTestTask().configure(task -> task.shouldRunAfter("test")));

            suite.getDependencies()
                    .getImplementation()
                    .add(target.getDependencies().project(Map.of("path", target.getPath())));
        });

        testing.getSuites().register("integrationTest", JvmTestSuite.class, suite -> {
            String implConfName = suite.getSources().getImplementationConfigurationName();
            target.getConfigurations()
                    .named(implConfName)
                    .configure(conf -> conf.extendsFrom(
                            target.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)));

            suite.getTargets().all(t -> t.getTestTask().configure(task -> task.shouldRunAfter("functionalTest")));

            suite.getDependencies()
                    .getImplementation()
                    .add(target.getDependencies().project(Map.of("path", target.getPath())));
        });

        testing.getSuites().register("performanceTest", JvmTestSuite.class, suite -> {
            String implConfName = suite.getSources().getImplementationConfigurationName();
            target.getConfigurations()
                    .named(implConfName)
                    .configure(conf -> conf.extendsFrom(
                            target.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)));

            suite.getTargets().all(t -> t.getTestTask().configure(task -> task.shouldRunAfter("test")));

            suite.getDependencies()
                    .getImplementation()
                    .add(target.getDependencies().project(Map.of("path", target.getPath())));
        });
    }
}

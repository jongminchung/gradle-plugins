package io.github.jongmin_chung.gradle.convention;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.testing.base.TestingExtension;
import org.jspecify.annotations.NonNull;

public class JvmTestSuiteConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void apply(Project target) {
        var testing = target.getExtensions().findByType(TestingExtension.class);
        if (testing == null) {
            return;
        }

        target.getExtensions().configure(TestingExtension.class, ext -> {
            ext.getSuites().configureEach(suite -> {
                if (suite instanceof JvmTestSuite jvmTestSuite) {
                    jvmTestSuite.useJUnitJupiter();
                }
            });

            ext.getSuites().register("functionalTest", JvmTestSuite.class, suite -> {
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

            ext.getSuites().register("integrationTest", JvmTestSuite.class, suite -> {
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

            ext.getSuites().register("performanceTest", JvmTestSuite.class, suite -> {
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
        });
    }
}

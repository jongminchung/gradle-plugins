package io.github.jongminchung.gradle.convention;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.base.TestingExtension;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
public class JvmTestSuiteConventionPlugin implements Plugin<@NonNull Project> {
    private static final List<String> DEFAULT_JVM_ARGS = List.of("-XX:+ShowCodeDetailsInExceptionMessages");
    private static final String SPRING_PROFILE_ARG_PREFIX = "-Dspring.profiles.active=";

    private static final String INTEGRATION_TEST_SUITE = "integrationTest";
    private static final String PERFORMANCE_TEST_SUITE = "performanceTest";

    @Override
    public void apply(Project target) {
        /*
         * afterEvaluate를 사용하지 않음!
         *
         * 이유: TestSuite의 Configuration(예: functionalTestImplementation)이
         * 프로젝트 평가 시점에 생성되어야 build.gradle.kts에서
         * dependencies 블록에서 사용할 수 있기 때문입니다.
         *
         * afterEvaluate를 사용하면:
         * ❌ build.gradle.kts에서 functionalTestImplementation을 찾을 수 없음
         * ✅ 사용하지 않으면 즉시 configuration이 생성됨
         */
        var testing = target.getExtensions().findByType(TestingExtension.class);
        if (testing != null) {
            configureTestSuites(target, testing);
        }
    }

    private static void configureTestSuites(Project project, TestingExtension testing) {
        testing.getSuites().configureEach(suite -> {
            if (suite instanceof JvmTestSuite jvmTestSuite) {
                jvmTestSuite.useJUnitJupiter();
            }
        });

        project.getTasks().named("test", Test.class).configure(task -> task.jvmArgs(jvmArgsForTestSuite("test")));
        registerTestSuite(project, testing, INTEGRATION_TEST_SUITE);
        registerTestSuite(project, testing, PERFORMANCE_TEST_SUITE);
    }

    private static void registerTestSuite(Project project, TestingExtension testing, String testSuiteName) {
        testing.getSuites().register(testSuiteName, JvmTestSuite.class, suite -> {
            String implConfName = suite.getSources().getImplementationConfigurationName();
            project.getConfigurations()
                    .named(implConfName)
                    .configure(conf -> conf.extendsFrom(
                            project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)));

            suite.getTargets()
                    .all(t -> t.getTestTask().configure(task -> task.jvmArgs(jvmArgsForTestSuite(testSuiteName))));

            suite.getDependencies()
                    .getImplementation()
                    .add(project.getDependencies().project(Map.of("path", project.getPath())));
        });
    }

    private static List<String> jvmArgsForTestSuite(String testSuiteName) {
        var jvmArgs = new ArrayList<>(DEFAULT_JVM_ARGS);
        jvmArgs.add(SPRING_PROFILE_ARG_PREFIX + testSuiteName);
        return jvmArgs;
    }
}

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

/**
 * JVM 테스트 스위트(Test Suite) 설정을 위한 컨벤션 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 기능을 수행합니다:
 * <ul>
 *   <li>모든 테스트 스위트에서 JUnit Jupiter를 사용하도록 설정합니다.</li>
 *   <li>기본 {@code test} 태스크 및 추가 등록된 테스트 스위트에 공통 JVM 인자를 추가합니다.</li>
 *   <li>{@code integrationTest} 및 {@code performanceTest} 스위트를 자동으로 등록합니다.</li>
 *   <li>각 테스트 스위트가 메인 소스셋의 의존성을 상속받도록 설정합니다.</li>
 * </ul>
 */
@SuppressWarnings("UnstableApiUsage")
public class JvmTestSuiteConventionPlugin implements Plugin<@NonNull Project> {
    private static final List<String> DEFAULT_JVM_ARGS = List.of("-XX:+ShowCodeDetailsInExceptionMessages");
    private static final String SPRING_PROFILE_ARG_PREFIX = "-Dspring.profiles.active=";

    private static final String INTEGRATION_TEST_SUITE = "integrationTest";
    private static final String PERFORMANCE_TEST_SUITE = "performanceTest";

    /**
     * JVM 테스트 스위트 설정을 프로젝트에 적용합니다.
     *
     * <p>이 메서드는 {@code afterEvaluate}를 사용하지 않습니다.
     * 테스트 스위트의 구성(Configuration, 예: {@code functionalTestImplementation})이
     * 프로젝트 평가 시점에 생성되어야 {@code build.gradle.kts}의 {@code dependencies} 블록에서 사용할 수 있기 때문입니다.
     *
     * @param target 적용 대상 프로젝트
     */
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

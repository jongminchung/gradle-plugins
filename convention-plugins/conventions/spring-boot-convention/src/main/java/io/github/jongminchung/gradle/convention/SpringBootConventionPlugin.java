package io.github.jongminchung.gradle.convention;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.JavaPlugin;
import org.jspecify.annotations.NonNull;

/**
 * Spring Boot 프로젝트를 위한 공통 컨벤션 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 기능을 수행합니다:
 * <ul>
 *   <li>{@code spring-boot-starter} 및 테스트 관련 의존성을 자동으로 추가합니다.</li>
 *   <li>JUnit 5 사용을 위해 JUnit Vintage 엔진을 제외합니다.</li>
 *   <li>{@code bootBuildInfo} 태스크를 설정하여 빌드 정보를 생성하도록 합니다.</li>
 *   <li>{@code bootBuildInfo} 태스크가 자바 컴파일 이후에 실행되도록 의존 관계를 설정합니다.</li>
 * </ul>
 */
public class SpringBootConventionPlugin implements Plugin<@NonNull Project> {
    private static final String SPRING_BOOT_PLUGIN_ID = "org.springframework.boot";
    private static final String BOOT_BUILD_INFO_TASK_NAME = "bootBuildInfo";
    private static final String SPRING_BOOT_STARTER = "org.springframework.boot:spring-boot-starter";
    private static final String SPRING_BOOT_STARTER_TEST = "org.springframework.boot:spring-boot-starter-test";
    private static final String JUNIT_VINTAGE_GROUP = "org.junit.vintage";
    private static final String JUNIT_VINTAGE_ENGINE = "junit-vintage-engine";
    private static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";
    private static final String SPRING_BOOT_EXTENSION_NAME = "springBoot";

    /**
     * Spring Boot 컨벤션을 프로젝트에 적용합니다.
     *
     * <p>이 메서드는 {@code afterEvaluate}를 사용하지 않고 {@code withId}를 사용하여
     * Spring Boot 플러그인이 적용되는 시점에 설정을 수행합니다.
     *
     * @param target 적용 대상 프로젝트
     */
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

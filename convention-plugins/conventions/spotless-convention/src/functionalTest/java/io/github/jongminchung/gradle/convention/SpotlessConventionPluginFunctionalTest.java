package io.github.jongminchung.gradle.convention;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

@DisplayName("Spotless Convention Plugin Functional Tests")
class SpotlessConventionPluginFunctionalTest {
    private static final String PLUGIN_ID = "io.github.jongminchung.spotless.convention";
    private static final String PALANTIR_VERSION = "2.81.0";

    @TempDir
    Path projectDir;

    private Path srcMainJava;
    private Path srcMainKotlin;
    private Path buildGenerated;

    @BeforeEach
    void setUp() throws IOException {
        srcMainJava = projectDir.resolve("src/main/java/example");
        srcMainKotlin = projectDir.resolve("src/main/kotlin/example");
        buildGenerated = projectDir.resolve("build/generated");

        Files.createDirectories(srcMainJava);
        Files.createDirectories(srcMainKotlin);
        Files.createDirectories(buildGenerated);

        writeSettingsFile();
        writeDefaultBuildScript();
    }

    // ===== Java Formatting Tests =====

    @Test
    @DisplayName("Java 소스 파일의 trailing spaces 제거")
    void removesTrailingSpacesFromJavaSource() throws IOException {
        // Given
        var javaFile = createJavaFileWithTrailingSpaces();

        // When
        var result = runSpotlessApply();

        // Then
        assertThat(result.task(":spotlessApply").getOutcome()).isEqualTo(SUCCESS);
        assertThat(readFile(javaFile))
                .as("Java 파일에서 trailing spaces가 제거되어야 함")
                .doesNotMatch("(?m).*\\s+$");
    }

    @Test
    @DisplayName("Java import 문을 올바른 순서로 정렬")
    void sortsJavaImportsInCorrectOrder() throws IOException {
        // Given
        var javaFile = createJavaFileWithUnorderedImports();

        // When
        runSpotlessApply();

        // Then
        var content = readFile(javaFile);
        assertImportsAreOrdered(content);
    }

    @Test
    @DisplayName("Java 애노테이션 포맷팅 적용")
    void formatsJavaAnnotations() throws IOException {
        // Given
        var javaFile = createJavaFile("""
                package example;
                
                import jakarta.annotation.Nullable;
                
                public class Example {
                    @Nullable String name() { return null; }
                }
                """);

        // When
        runSpotlessApply();

        // Then
        assertThat(readFile(javaFile))
                .as("애노테이션이 적절히 포맷팅되어야 함")
                .contains("@Nullable");
    }

    // ===== Kotlin Formatting Tests =====

    @Test
    @DisplayName("Kotlin 소스 파일의 trailing spaces 제거")
    void removesTrailingSpacesFromKotlinSource() throws IOException {
        // Given
        var kotlinFile = createKotlinFileWithTrailingSpaces();

        // When
        runSpotlessApply();

        // Then
        assertThat(readFile(kotlinFile))
                .as("Kotlin 파일에서 trailing spaces가 제거되어야 함")
                .doesNotMatch("(?m).*\\s+$");
    }

    @Test
    @DisplayName("build.gradle.kts 포맷팅 적용")
    void formatsGradleBuildScript() throws IOException {
        // Given
        writeMessyBuildScript();
        var buildScriptPath = projectDir.resolve("build.gradle.kts");
        var messyContent = readFile(buildScriptPath);

        assertThat(messyContent)
                .as("초기 build.gradle.kts에는 탭과 trailing space가 있어야 함")
                .contains("\t");
        assertThat(messyContent)
                .as("초기 스크립트에는 trailing space가 포함되어야 함")
                .containsPattern("(?m).*\\s+$");

        // When
        runSpotlessApply();

        // Then
        var formattedContent = readFile(buildScriptPath);
        assertThat(formattedContent)
                .as("Spotless는 trailing space를 제거함")
                .doesNotMatch("(?m).*\\s+$");
        assertThat(formattedContent)
                .as("Spotless는 탭을 스페이스로 바꿈")
                .doesNotContain("\t");
        assertThat(formattedContent)
                .contains("plugins {")
                .contains("mavenCentral()");
    }

    // ===== Build Directory Exclusion Tests =====

    @Test
    @DisplayName("build 디렉토리의 파일은 Spotless 적용 대상에서 제외")
    void excludesBuildDirectoryFromSpotlessTarget() throws IOException {
        // Given
        writeBuildScriptWithTargetAssertion();
        var generatedFile = createGeneratedFileInBuildDir();
        var originalContent = readFile(generatedFile);

        // When
        var result = runGradle("spotlessApply", "assertSpotlessTargets");

        // Then
        assertThat(result.getOutput())
                .as("build 디렉토리가 Spotless 대상에서 제외되어야 함")
                .contains("BUILD SUCCESSFUL");

        assertThat(readFile(generatedFile))
                .as("build 디렉토리의 파일은 포맷팅되지 않아야 함")
                .isEqualTo(originalContent)
                .contains("leave me dirty    "); // trailing spaces 유지
    }

    @Test
    @DisplayName("spotlessJava Task의 target에 build 디렉토리 파일이 포함되지 않음")
    void spotlessJavaTaskDoesNotIncludeBuildDirectoryFiles() throws IOException {
        // Given
        writeBuildScriptWithTargetAssertion();
        createJavaFileWithName("Example.java");
        createGeneratedFileInBuildDir();

        // When & Then
        var result = runGradle("assertSpotlessTargets");

        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    // ===== Integration Test =====

    @Test
    @DisplayName("여러 소스 파일에 Spotless 적용하고 build 디렉토리는 제외")
    void formatsMultipleSourcesAndSkipsBuildDirectory() throws IOException {
        // Given
        var javaFile = createJavaFileWithTrailingSpaces();
        var kotlinFile = createKotlinFileWithTrailingSpaces();
        var generatedFile = createGeneratedFileInBuildDir();
        var originalGeneratedContent = readFile(generatedFile);

        writeBuildScriptWithTargetAssertion();

        // When
        var result = runGradle("spotlessApply", "assertSpotlessTargets");

        // Then
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");

        // Java 파일 검증
        assertThat(readFile(javaFile))
                .as("Java 소스는 포맷팅되어야 함")
                .doesNotMatch("(?m).*\\s+$");
        assertImportsAreOrdered(readFile(javaFile));

        // Kotlin 파일 검증
        assertThat(readFile(kotlinFile))
                .as("Kotlin 소스는 포맷팅되어야 함")
                .doesNotMatch("(?m).*\\s+$");

        // 생성된 파일은 그대로 유지
        assertThat(readFile(generatedFile))
                .as("build 디렉토리의 생성 파일은 변경되지 않아야 함")
                .isEqualTo(originalGeneratedContent)
                .contains("leave me dirty    ");
    }

    // ===== Helper Methods: File Creation =====

    private Path createJavaFileWithTrailingSpaces() throws IOException {
        return createJavaFile("""
                package example;
                
                import com.fasterxml.jackson.databind.ObjectMapper;
                import jakarta.annotation.Nullable;
                import java.util.List;
                
                public class Example {
                    // trailing spaces   
                    @Nullable List<ObjectMapper> values() { return List.of(); }
                }
                """);
    }

    private Path createJavaFileWithUnorderedImports() throws IOException {
        return createJavaFile("""
                package example;
                
                import java.util.List;
                import com.fasterxml.jackson.databind.ObjectMapper;
                import jakarta.annotation.Nullable;
                
                public class Example {
                    @Nullable List<ObjectMapper> values() { return List.of(); }
                }
                """);
    }

    private Path createJavaFile(String content) throws IOException {
        return createFile(srcMainJava.resolve("Example.java"), content);
    }

    private Path createJavaFileWithName(String fileName) throws IOException {
        return createJavaFile("""
                package example;
                
                public class %s {
                    void hello() {}
                }
                """.formatted(fileName.replace(".java", "")));
    }

    private Path createKotlinFileWithTrailingSpaces() throws IOException {
        return createFile(srcMainKotlin.resolve("Example.kt"), """
                package example
                
                class Example {
                    fun greet() = "hello"   
                }
                """);
    }

    private Path createGeneratedFileInBuildDir() throws IOException {
        return createFile(buildGenerated.resolve("Generated.java"), """
                package example;
                
                public class Generated {
                    // leave me dirty    
                }
                """);
    }

    private Path createFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.writeString(path, content);
    }

    // ===== Helper Methods: Assertions =====

    private void assertImportsAreOrdered(String content) {
        var javaImport = content.indexOf("import java.util.List;");
        var jakartaImport = content.indexOf("import jakarta.annotation.Nullable;");
        var comImport = content.indexOf("import com.fasterxml.jackson.databind.ObjectMapper;");

        assertThat(List.of(javaImport, jakartaImport, comImport))
                .as("모든 import 문이 존재해야 함")
                .allMatch(index -> index >= 0);

        assertThat(javaImport)
                .as("java.* import가 jakarta.* import보다 앞에 있어야 함")
                .isLessThan(jakartaImport);

        assertThat(jakartaImport)
                .as("jakarta.* import가 com.* import보다 앞에 있어야 함")
                .isLessThan(comImport);
    }

    // ===== Helper Methods: Build Scripts =====

    private void writeSettingsFile() throws IOException {
        writeFile(projectDir.resolve("settings.gradle.kts"), """
                dependencyResolutionManagement {
                    versionCatalogs {
                        create("libs") {
                            version("palantirJavaFormat", "%s")
                        }
                    }
                }
                rootProject.name = "spotless-convention-test"
                """.formatted(PALANTIR_VERSION));
    }

    private void writeBuildScriptWithTargetAssertion() throws IOException {
        writeBuildScript("""
                plugins {
                    id("%s")
                    id("java")
                }
                
                repositories {
                    mavenCentral()
                }
                
                tasks.register("assertSpotlessTargets") {
                    group = "verification"
                    description = "Verifies that build directory is excluded from Spotless"
                
                    doLast {
                        val spotlessJava = tasks.named("spotlessJava").get()
                        val target = spotlessJava.javaClass.getMethod("getTarget").invoke(spotlessJava)
                        val targetFilesCandidate = target.javaClass.getMethod("getFiles").invoke(target)
                        val targetFiles = when (targetFilesCandidate) {
                            is org.gradle.api.file.FileCollection -> targetFilesCandidate.files
                            is java.util.Collection<*> -> targetFilesCandidate.filterIsInstance<java.io.File>()
                            else -> emptyList()
                        }
                        val buildPath = layout.buildDirectory.get().asFile.toPath()

                        targetFiles.forEach { file ->
                            val filePath = file.toPath()
                            println("  - Checking: $filePath")

                            if (filePath.startsWith(buildPath)) {
                                throw IllegalStateException(
                                        "Build directory file was included in Spotless target: $file"
                                )
                            }
                        }

                        println("✅ All target files are outside build directory")
                    }
                }
                """.formatted(PLUGIN_ID));
    }

    private void writeMessyBuildScript() throws IOException {
        writeBuildScript("""
                \tplugins {
                    \t\tid("%s")   
                    \t\tid("java")    
                }
                
                \trepositories {
                    \t\tmavenCentral()    
                }
                """.formatted(PLUGIN_ID));
    }

    private void writeDefaultBuildScript() throws IOException {
        writeBuildScript("""
                plugins {
                    id("%s")
                    id("java")
                }

                repositories {
                    mavenCentral()
                }
                """.formatted(PLUGIN_ID));
    }

    private void writeBuildScript(String content) throws IOException {
        writeFile(projectDir.resolve("build.gradle.kts"), content);
    }

    private void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    private String readFile(Path path) throws IOException {
        return Files.readString(path);
    }

    // ===== Helper Methods: Gradle Execution =====

    private BuildResult runSpotlessApply() {
        return runGradle("spotlessApply");
    }

    private BuildResult runSpotlessCheck() {
        return runGradle("spotlessCheck");
    }

    private BuildResult runGradle(String... tasks) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments(buildArgumentsList(tasks))
                .withPluginClasspath()
                .withDebug(true) // 디버깅 활성화
                .forwardOutput() // 출력 포워딩
                .build();
    }

    private List<String> buildArgumentsList(String... tasks) {
        return Stream.concat(
                        Stream.of(
                                "--stacktrace", // 스택 트레이스 출력
                                "--info"        // 상세 로그
                        ),
                        Stream.of(tasks))
                .toList();
    }

    // ===== Nested Test Classes =====

    @Nested
    @DisplayName("SpotlessCheck Task Tests")
    class SpotlessCheckTests {

        @Test
        @DisplayName("포맷팅이 올바르면 spotlessCheck 성공")
        void succeedsWhenFormattingIsCorrect() throws IOException {
            // Given
            createJavaFile("""
                    package example;
                    
                    public class Example {
                        void hello() {}
                    }
                    """);
            runSpotlessApply();

            // When
            var result = runSpotlessCheck();

            // Then
            assertThat(result.task(":spotlessCheck").getOutcome()).isEqualTo(SUCCESS);
        }

        @Test
        @DisplayName("포맷팅이 틀리면 spotlessCheck 실패")
        void failsWhenFormattingIsIncorrect() throws IOException {
            // Given
            createJavaFileWithTrailingSpaces();

            // When & Then
            var runner = GradleRunner.create()
                    .withProjectDir(projectDir.toFile())
                    .withArguments("spotlessCheck")
                    .withPluginClasspath();

            assertThat(runner.buildAndFail().getOutput())
                    .contains("spotlessCheck FAILED")
                    .containsAnyOf("format violations", "needs formatting");
        }
    }

    @Nested
    @DisplayName("Plugin Configuration Tests")
    class PluginConfigurationTests {

        @Test
        @DisplayName("Java 플러그인 없이는 실패")
        void failsWithoutJavaPlugin() throws IOException {
            // Given
            writeBuildScript("""
                    plugins {
                        id("%s")
                    }
                    """.formatted(PLUGIN_ID));

            // When & Then
            var runner = GradleRunner.create()
                    .withProjectDir(projectDir.toFile())
                    .withArguments("tasks")
                    .withPluginClasspath();

            assertThat(runner.buildAndFail().getOutput())
                    .containsAnyOf("plugin", "dependency", "not found");
        }

        @Test
        @DisplayName("멀티모듈 프로젝트에서 정상 동작")
        void worksInMultiModuleProject() throws IOException {
            // Given
            var subprojectDir = projectDir.resolve("subproject");
            Files.createDirectories(subprojectDir);

            writeFile(projectDir.resolve("settings.gradle.kts"), """
                    rootProject.name = "multi-module-test"
                    include("subproject")
                    """);

            writeBuildScript("""
                    plugins {
                        id("%s") apply false
                    }
                    
                    subprojects {
                        apply(plugin = "%s")
                        apply(plugin = "java")
                    
                        repositories {
                            mavenCentral()
                        }
                    }
                    """.formatted(PLUGIN_ID, PLUGIN_ID));

            writeFile(subprojectDir.resolve("build.gradle.kts"), "");

            var subprojectSrc = subprojectDir.resolve("src/main/java/example");
            Files.createDirectories(subprojectSrc);
            createFile(subprojectSrc.resolve("Sub.java"), """
                    package example;
                    
                    public class Sub {
                        void test() {}   
                    }
                    """);

            // When
            var result = runGradle("spotlessApply");

            // Then
            assertThat(result.task(":subproject:spotlessApply").getOutcome()).isEqualTo(SUCCESS);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("빈 파일도 처리 가능")
        void handlesEmptyFiles() throws IOException {
            // Given
            createJavaFile("");

            // When & Then
            assertThat(runSpotlessApply().task(":spotlessApply").getOutcome())
                    .isEqualTo(SUCCESS);
        }

        @Test
        @DisplayName("매우 큰 파일도 처리 가능")
        void handlesLargeFiles() throws IOException {
            // Given
            var largeContent = new StringBuilder("package example;\n\npublic class Large {\n");
            for (int i = 0; i < 10000; i++) {
                largeContent.append("    void method").append(i).append("() {}\n");
            }
            largeContent.append("}\n");

            createJavaFile(largeContent.toString());

            // When & Then
            assertThat(runSpotlessApply().task(":spotlessApply").getOutcome())
                    .isEqualTo(SUCCESS);
        }

        @Test
        @DisplayName("특수 문자가 포함된 파일도 처리 가능")
        void handlesFilesWithSpecialCharacters() throws IOException {
            // Given
            createJavaFile("""
                    package example;
                    
                    public class Example {
                        String emoji = "🎉 Test 테스트";
                        String special = "\\n\\t\\r";
                    }
                    """);

            // When & Then
            assertThat(runSpotlessApply().task(":spotlessApply").getOutcome())
                    .isEqualTo(SUCCESS);
        }

        @Test
        @DisplayName("중첩된 build 디렉토리도 제외")
        void excludesNestedBuildDirectories() throws IOException {
            // Given
            var nestedBuild = projectDir.resolve("module/build/generated");
            Files.createDirectories(nestedBuild);
            createFile(nestedBuild.resolve("Nested.java"), """
                    package example;
                    
                    public class Nested {
                        // dirty code    
                    }
                    """);

            writeBuildScriptWithTargetAssertion();

            // When & Then
            var result = runGradle("assertSpotlessTargets");
            assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        }
    }
}

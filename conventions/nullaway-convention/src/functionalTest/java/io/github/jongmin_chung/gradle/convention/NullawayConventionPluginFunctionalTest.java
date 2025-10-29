package io.github.jongmin_chung.gradle.convention;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NullawayConventionPluginFunctionalTest {

    @TempDir
    File testProjectDir;

    @Test
    void pluginAppliesNullawayAndJspecifyAndFailsOnNullUnsafeCode() throws IOException {
        File buildFile = new File(testProjectDir, "build.gradle.kts");
        File settingsFile = new File(testProjectDir, "settings.gradle.kts");

        File sourceDir = new File(testProjectDir, "src/main/java/com/example");
        sourceDir.mkdirs();
        File sourceFile = new File(sourceDir, "MyClass.java");

        try (FileWriter settingsWriter = new FileWriter(settingsFile)) {
            settingsWriter.write("pluginManagement {\n" + "    repositories {\n"
                    + "        gradlePluginPortal()\n"
                    + "        mavenCentral()\n"
                    + "    }\n"
                    + "}\n"
                    + "\n"
                    + "rootProject.name = \"test-project\"\n"
                    + "\n"
                    + "dependencyResolutionManagement {\n"
                    + "    versionCatalogs {\n"
                    + "        create(\"libs\") {\n"
                    + "            library(\"jspecify\", \"org.jspecify\", \"jspecify\").version(\"1.0.0\")\n"
                    + "            library(\"nullaway\", \"com.uber.nullaway\", \"nullaway\").version(\"0.12.10\")\n"
                    + "        }\n"
                    + "    }\n"
                    + "}\n");
        }

        try (FileWriter buildWriter = new FileWriter(buildFile)) {
            buildWriter.write("plugins {\n" + "    java\n"
                    + "    id(\"io.github.jongmin-chung.nullaway.convention\")\n"
                    + "}\n"
                    + "\n"
                    + "group = \"com.example\"\n"
                    + "version = \"0.0.1\"\n"
                    + "\n"
                    + "repositories {\n"
                    + "    mavenCentral()\n"
                    + "}\n");
        }

        try (FileWriter sourceWriter = new FileWriter(sourceFile)) {
            sourceWriter.write("package com.example;\n" + "\n"
                    + "import org.jspecify.annotations.Nullable;\n"
                    + "\n"
                    + "public class MyClass {\n"
                    + "    public String returnsNonNull() {\n"
                    + "        return null; // This should fail the build\n"
                    + "    }\n"
                    + "\n"
                    + "    @Nullable\n"
                    + "    public String returnsNullable() {\n"
                    + "        return null; // This is fine\n"
                    + "    }\n"
                    + "}\n");
        }

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace")
                .withPluginClasspath();

        BuildResult result = runner.buildAndFail();

        // Print the output for debugging
        System.err.println("---- BUILD FAILED ----");
        System.err.println(result.getOutput());
        System.err.println("----------------------");

        assertEquals(TaskOutcome.FAILED, result.task(":compileJava").getOutcome());
        assertTrue(result.getOutput()
                .contains("[NullAway] returning @Nullable expression from method with @NonNull return type"));
    }
}

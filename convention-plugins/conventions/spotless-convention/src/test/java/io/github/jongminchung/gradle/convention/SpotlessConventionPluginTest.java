package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;

class SpotlessConventionPluginTest {

    @Test
    void appliesSpotlessPluginAndRegistersExtension() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(SpotlessConventionPlugin.class);
        evaluate(project);

        assertThat(project.getPlugins().hasPlugin(SpotlessPlugin.class)).isTrue();
        assertThat(project.getExtensions().findByType(SpotlessExtension.class)).isNotNull();
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}

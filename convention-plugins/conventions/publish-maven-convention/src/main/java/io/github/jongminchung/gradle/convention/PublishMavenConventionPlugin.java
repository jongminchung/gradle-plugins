package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.jspecify.annotations.NonNull;

/**
 * Maven Central 배포 설정을 프로젝트에 적용하는 플러그인입니다.
 *
 * <p>이 플러그인은 다음과 같은 기능을 수행합니다:
 * <ul>
 *   <li>{@code maven-publish} 플러그인이 적용된 경우 동작합니다.</li>
 *   <li>{@link ExtraMavenPublishExtension}을 통해 배포 활성화 여부를 제어합니다.</li>
 *   <li>모든 배포물(Publication)에 빌드 식별자(Build Identifier)를 포함하도록 설정합니다.</li>
 * </ul>
 */
public class PublishMavenConventionPlugin implements Plugin<@NonNull Project> {

    /**
     * Maven 배포 설정을 프로젝트에 적용합니다.
     *
     * <p>이 메서드는 {@code afterEvaluate}를 사용하지 않고 {@code withPlugin}을 사용하여
     * {@code maven-publish} 플러그인이 적용되는 시점에 설정을 수행합니다.
     *
     * @param target 적용 대상 프로젝트
     */
    @Override
    public void apply(Project target) {
        var extraMavenPublishExtension = target.getExtensions()
                .create(
                        ExtraMavenPublishExtension.EXTRA_MAVEN_PUBLISH_EXTENSION_NAME,
                        ExtraMavenPublishExtension.class);

        target.getPluginManager().withPlugin("maven-publish", plugin -> {
            if (!extraMavenPublishExtension.getEnabled().getOrElse(true)) {
                return;
            }

            var publishing = target.getExtensions().findByType(PublishingExtension.class);

            if (publishing != null) {
                var publications = publishing.getPublications();

                publications.all(Publication::withBuildIdentifier);
            }
        });
    }
}

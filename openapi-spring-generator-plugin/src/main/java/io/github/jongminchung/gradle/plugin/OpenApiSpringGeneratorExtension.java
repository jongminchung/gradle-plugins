package io.github.jongminchung.gradle.plugin;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.jspecify.annotations.NonNull;

public interface OpenApiSpringGeneratorExtension {
    /** 번들의 entry 포인트가 되는 OpenAPI 파일 */
    RegularFileProperty getInputFile();

    /** bundle 결과가 생성될 OpenAPI 파일 */
    RegularFileProperty getOutputFile();

    /** OpenAPI Generator에 전달할 additionalProperties */
    MapProperty<@NonNull String, @NonNull String> getAdditionalProperties();
}

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

interface SwaggerMergerExtension {
    @get:InputFile
    @get:Optional
    val inputFile: RegularFileProperty

    @get:Input
    @get:Optional
    val outputFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    val configFile: RegularFileProperty

    @get:Input
    @get:Optional
    val additionalArgs: ListProperty<String>

    @get:Input
    @get:Optional
    val npxCommand: Property<String>

    @get:Input
    @get:Optional
    val workingDirectory: Property<String>
}

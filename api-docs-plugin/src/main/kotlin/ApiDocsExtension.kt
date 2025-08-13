import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty

interface ApiDocsExtension {
    val sourceDir: DirectoryProperty
    val htmlDir: DirectoryProperty
    val postmanVariables: MapProperty<String, String>
    val postmanConfigFile: RegularFileProperty
}

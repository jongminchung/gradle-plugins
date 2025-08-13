import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpxTask
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*

class ApiDocsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(NodePlugin::class.java)
        project.extensions.configure<NodeExtension> {
            download.set(true)
            version.set("22.18.0")
            workDir.set(project.layout.buildDirectory.dir("nodejs"))
        }

        // Create extension for configuration
        val extension = project.extensions.create<ApiDocsExtension>("apiDocs")

        // Configure default values
        extension.apply {
            sourceDir.convention(project.layout.projectDirectory.dir("yaml"))
            htmlDir.convention(project.layout.projectDirectory.dir("html"))
            postmanVariables.convention(emptyMap())
            postmanConfigFile.convention(project.layout.projectDirectory.file("postman.json"))
        }

        project.tasks.apply {
            withType(NpxTask::class).configureEach {
                dependsOn(named<NpmInstallTask>("npmInstall"))
            }

            val lintOAS = register<NpxTask>("lintOAS") {
                group = GROUP_NAME

                command.set("npx")

                val source = extension.sourceDir

                val arguments = objects.listProperty(String::class)
                arguments.add("@redocly/cli")
                arguments.add("lint")
                arguments.add(source.file(OAS_FILE_NAME).get().asFile.path)

                args.set(arguments)

                inputs.dir(source)
            }

            val bundleOAS = register<NpxTask>("bundleOAS") {
                group = GROUP_NAME
                dependsOn(lintOAS)

                command.set("npx")

                val source = extension.sourceDir
                val output = project.layout.buildDirectory.file(GENERATED_OAS_PATH)

                val arguments = objects.listProperty(String::class)

                arguments.add("@redocly/cli")
                arguments.add("bundle")
                arguments.add(source.file(OAS_FILE_NAME).get().asFile.path)
                arguments.add("-o")
                arguments.add(output.map { it.asFile.path })

                args.set(arguments)

                inputs.dir(source)
                outputs.file(output)
            }

            val bundleRedoc = register<NpxTask>("bundleRedoc") {
                group = GROUP_NAME
                dependsOn(bundleOAS)

                command.set("npx")

                val output = project.layout.buildDirectory.file(GENERATED_REDOC_PATH)

                val tempConfigFile = project.layout.buildDirectory.file("tmp/redocly.yml")
                val tempTemplateFile = project.layout.buildDirectory.file("tmp/templates.hbs")

                doFirst {
                    val resourceStream = javaClass.classLoader.getResourceAsStream("redocly.yml")
                    resourceStream.use { input ->
                        tempConfigFile.get().asFile.outputStream().use { output ->
                            input?.copyTo(output)
                        }
                    }
                    val templateStream = javaClass.classLoader.getResourceAsStream("templates.hbs")
                    templateStream.use { input ->
                        tempTemplateFile.get().asFile.outputStream().use { output ->
                            input?.copyTo(output)
                        }
                    }
                }

                val arguments = objects.listProperty(String::class)
                arguments.add("@redocly/cli")
                arguments.add("build-docs")
                arguments.add(bundleOAS.map { it.outputs.files.singleFile.path })
                arguments.add("-o")
                arguments.add(output.map { it.asFile.path })
                arguments.add("--config")
                arguments.add(tempConfigFile.map { it.asFile.path })
                arguments.add("--template")
                arguments.add(tempTemplateFile.map { it.asFile.path })
                args.set(arguments)

                doLast {
                    logger.lifecycle("Redoc bundle created \uD83C\uDF89")
                }
            }

            val filterOAS = register<Copy>("filterOAS") {
                group = GROUP_NAME

                from(bundleOAS.map { it.outputs.files.singleFile })
                into(project.layout.buildDirectory.dir(GENERATED_OAS_FILTERED_PATH))

                val properties = extension.postmanVariables.get()

                inputs.properties(properties)

                properties.forEach { (key, value) ->
                    filter {
                        it.replace(value, "\"{{$key}}\"")
                    }
                }
            }

            val generatePostman = register<NpxTask>("generatePostman") {
                group = GROUP_NAME
                dependsOn(filterOAS)

                command.set("npx")

                inputs.file(filterOAS.map { it.outputs.files.singleFile.resolve(OAS_FILE_NAME) })
                inputs.file(extension.postmanConfigFile.get().asFile.path)

                val output = project.layout.buildDirectory.file(GENERATED_POSTMAN_PATH)
                outputs.file(output)

                val arguments = objects.listProperty(String::class)

                arguments.add("openapi-to-postmanv2")
                arguments.add("-s")
                arguments.add(filterOAS.map { it.outputs.files.singleFile.resolve(OAS_FILE_NAME).path })
                arguments.add("-o")
                arguments.add(output.map { it.asFile.path })
                arguments.add("-c")
                arguments.add(extension.postmanConfigFile.get().asFile.path)

                args.set(arguments)

                doLast {
                    @Suppress("UNCHECKED_CAST")
                    val postman = JsonSlurper().parse(output.get().asFile) as Map<String, Any>

                    val modified = postman.toMutableMap()

                    @Suppress("UNCHECKED_CAST")
                    val variable = modified["variable"] as MutableList<Any>

                    val properties = extension.postmanVariables.get()

                    properties.forEach { (key, value) ->
                        variable.add(mapOf("key" to key, "value" to value))
                    }

                    val jsonBuilder = JsonBuilder(modified)

                    output.get().asFile.writeText(jsonBuilder.toString())
                }
            }

            // 6. Create aggregate tasks for convenience
            register("buildAPIDocs") {
                group = GROUP_NAME
                description = "Build all API documentation"
                dependsOn(bundleOAS, bundleRedoc, filterOAS, generatePostman)
            }

            register<Delete>("cleanAPIDocs") {
                group = GROUP_NAME
                description = "Clean all API documentation build outputs"

                delete(project.layout.buildDirectory.dir("generated"))
            }
        }
    }

    companion object {
        const val GROUP_NAME = "API Documentation"
        const val OAS_FILE_NAME = "oas.yml"
        const val REDOC_FILE_NAME = "redoc.html"

        const val GENERATED_OAS_PATH = "generated/$OAS_FILE_NAME"
        const val GENERATED_REDOC_PATH = "generated/$REDOC_FILE_NAME"
        const val GENERATED_OAS_FILTERED_PATH = "generated/filtered"
        const val GENERATED_POSTMAN_PATH = "generated/postman.json"
    }
}

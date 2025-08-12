import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class SwaggerMergerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Extension 생성
        val extension = project.extensions.create<SwaggerMergerExtension>("swaggerMerger")

        // 기본값 설정
        extension.apply {
            npxCommand.convention("npx")
            workingDirectory.convention(project.layout.projectDirectory.toString())
        }

        // 기본 태스크 등록
        project.tasks.register<SwaggerMergerTask>("mergeSwagger") {
            description = "Merge Swagger specifications defined in swaggerMerger extension"

            // Extension 값들을 태스크에 연결
            inputFile.convention(extension.inputFile)
            outputFile.convention(extension.outputFile)
            configFile.convention(extension.configFile)
            additionalArgs.convention(extension.additionalArgs)
            npxCommand.convention(extension.npxCommand)
            workingDirectory.convention(project.layout.projectDirectory)

            // 입력/출력 파일이 설정되지 않으면 태스크 비활성화
            onlyIf {
                extension.inputFile.isPresent && extension.outputFile.isPresent
            }
        }

        // DSL로 여러 태스크를 쉽게 생성할 수 있는 컨테이너 제공
        project.extensions.create("swaggerMergerTasks", SwaggerMergerTaskContainer::class.java, project)
    }
}

// 여러 태스크를 쉽게 생성하기 위한 컨테이너
open class SwaggerMergerTaskContainer(private val project: Project) {
    fun create(
        name: String,
        inputPath: String,
        outputPath: String,
        config: String? = null,
        args: List<String> = emptyList()
    ) {
        project.tasks.register<SwaggerMergerTask>(name) {
            inputFile.set(project.file(inputPath))
            outputFile.set(project.file(outputPath))
            config?.let { configFile.set(project.file(it)) }
            additionalArgs.set(args)
        }
    }
}

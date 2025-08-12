import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject
@DisableCachingByDefault(because = "Always fetch latest swagger-merger")
abstract class SwaggerMergerTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val configFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val additionalArgs: ListProperty<String>

    @get:Input
    abstract val npxCommand: Property<String>

    @get:Internal
    abstract val workingDirectory: DirectoryProperty

    init {
        group = "swagger"
        description = "Merge Swagger/OpenAPI specifications using npx swagger-merger"

        // 기본값 설정
        npxCommand.convention("npx")
        additionalArgs.convention(emptyList())
        workingDirectory.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun execute() {
        // npx 사용 가능 여부 확인
        verifyNpxAvailable()

        // 출력 디렉토리 생성
        outputFile.get().asFile.parentFile.mkdirs()

        // swagger-merger 실행
        val commandLine = buildCommandLine()

        logger.lifecycle("📝 Merging Swagger specifications...")
        logger.info("Command: ${commandLine.joinToString(" ")}")

        val result = execOperations.exec {
            workingDir = workingDirectory.asFile.get()
            commandLine(commandLine)

            // 표준 출력 처리
            isIgnoreExitValue = true
            standardOutput = System.out
            errorOutput = System.err
        }

        if (result.exitValue != 0) {
            throw GradleException(
                "swagger-merger failed with exit code ${result.exitValue}. " +
                        "Make sure the input file exists and is valid."
            )
        }

        logger.lifecycle("✅ Successfully merged to: ${outputFile.get().asFile.absolutePath}")
    }

    private fun verifyNpxAvailable() {
        val npxCmd = npxCommand.get()

        val result = try {
            execOperations.exec {
                commandLine(npxCmd, "--version")
                standardOutput = org.gradle.internal.io.NullOutputStream.INSTANCE
                errorOutput = org.gradle.internal.io.NullOutputStream.INSTANCE
                isIgnoreExitValue = true
            }
        } catch (e: Exception) {
            throw GradleException(
                "❌ '$npxCmd' command not found. Please install Node.js and npm first.\n" +
                        "Visit https://nodejs.org/ for installation instructions."
            )
        }

        if (result.exitValue != 0) {
            throw GradleException(
                "❌ Failed to execute '$npxCmd --version'. " +
                        "Please ensure Node.js and npm are properly installed."
            )
        }
    }

    private fun buildCommandLine(): List<String> {
        val cmd = mutableListOf<String>()

        // npx 명령어
        cmd.add(npxCommand.get())

        // npx가 패키지를 자동으로 다운로드하고 실행하도록 설정
        cmd.add("--yes")  // 자동으로 설치 승인
        cmd.add("swagger-merger")

        // 입력 파일
        cmd.add("-i")
        cmd.add(inputFile.get().asFile.absolutePath)

        // 출력 파일
        cmd.add("-o")
        cmd.add(outputFile.get().asFile.absolutePath)

        // 설정 파일 (옵션)
        if (configFile.isPresent) {
            cmd.add("-c")
            cmd.add(configFile.get().asFile.absolutePath)
        }

        // 추가 인자들
        additionalArgs.get().forEach { arg ->
            cmd.add(arg)
        }

        return cmd
    }
}

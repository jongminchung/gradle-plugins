plugins {
    id("io.github.jongmin-chung.swagger-merger")
}

// 방법 1: Extension을 통한 기본 태스크 설정
swaggerMerger {
    inputFile.set(file("api-docs/api/openapi.yml"))
    outputFile.set(file("build/generated/openapi.yml"))

    // 선택적 설정
//    configFile.set(file("swagger-config.json"))
//    additionalArgs.set(listOf("--compact", "--verbose"))
}

plugins {
    id("io.github.jongmin-chung.api-docs-plugin")
}

apiDocs {
    sourceDir = layout.projectDirectory.dir("yaml")

    postmanVariables = (
        mapOf(
            "petId" to "123e4567-e89b-12d3-a456-426614174000",
        )
    )

    postmanConfigFile = layout.projectDirectory.file("postman.json")
}

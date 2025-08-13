rootProject.name = "gradle-plugins"

//includeBuild("build-logic")
includeBuild("swagger-merger-plugin")
includeBuild("api-docs-plugin")

include("test-oas")

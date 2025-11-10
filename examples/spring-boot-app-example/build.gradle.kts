buildscript {
    dependencies {
        // Put the plugin project on the buildscript classpath so the example works without publishing.
        classpath(project(":spring-boot-app-plugin"))
    }
}

apply(plugin = "io.github.jongminchung.spring-boot-app")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
}

tasks.register("printGroup") {
    println("Project group: ${project.group}")
}

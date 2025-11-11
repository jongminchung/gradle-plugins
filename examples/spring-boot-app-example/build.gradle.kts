plugins {
    id("io.github.jongminchung.spring-boot-app") version "0.0.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
}

import bilboka.dependencies.Libs
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")

    id("bilboka.plugin")
}

group = "bilboka"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":bilboka-messagebot"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(platform(Libs.okHttp))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    testImplementation("org.mockito:mockito-core:3.3.3") // TODO trenger vi mockito?
    testImplementation("io.mockk:mockk:1.10.6") // Feilet ved nyere versjon
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("com.squareup.okhttp3:mockwebserver")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

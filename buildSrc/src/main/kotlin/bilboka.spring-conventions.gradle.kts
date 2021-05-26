plugins {
    id("bilboka.java-conventions")

    kotlin("plugin.spring")
    id("org.springframework.boot")
}

println("Enabling Kotlin Spring plugin in project ${project.name}...")
apply(plugin = "org.jetbrains.kotlin.plugin.spring")

println("Enabling Spring Boot plugin in project ${project.name}...")
apply(plugin = "org.springframework.boot")

println("Enabling Spring Boot Dependency Management in project ${project.name}...")
apply(plugin = "io.spring.dependency-management")
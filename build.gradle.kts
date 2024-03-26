group = "io.github.apwlq.mealapi"
version = "2.1-Release"

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
    }
}

plugins {
    kotlin("jvm") version "1.9.23"
}

apply(plugin = "com.github.johnrengelman.shadow")
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("${rootProject.name}-$version.jar")
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
            "Main-Class" to "$group.${rootProject.name}Kt"
        ))
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    `maven-publish`
    application
}

group = "co.q64.faktorio"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")


    // ktor
    implementation("io.ktor:ktor-server-core:2.2.2")
    implementation("io.ktor:ktor-server-netty:2.2.2")
    implementation("io.ktor:ktor-server-auth:2.2.2")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.2")
    implementation("io.ktor:ktor-server-swagger:2.2.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-Xcontext-receivers",
        )
    }
}

application {
    mainClass.set("MainKt")
}

publishing {
    repositories {
        maven {
            name = "deotime"
            url = uri("https://repo.q64.io/deotime")
            credentials {
                username = "deotime"
                password = System.getenv("RAIN_REPO_PW")
            }
        }
    }
}
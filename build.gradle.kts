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

    // swagger
    implementation("io.swagger.core.v3:swagger-core:2.2.8")
    implementation("io.swagger.parser.v3:swagger-parser:2.1.11")

    // ktor
    val ktorVersion = "2.2.2"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.0")

    // serialization
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
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
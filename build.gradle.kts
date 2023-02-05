import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    `maven-publish`
    application
}

group = "co.q64"
version = "1.0.13"

repositories {
    mavenCentral()
}

dependencies {

    // swagger
    implementation("io.swagger.core.v3:swagger-models:2.2.8")

    // ktor
    val ktorVersion = "2.2.2"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.8.0")

    // serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")


    // testing
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    testImplementation("ch.qos.logback:logback-classic:1.3.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

application {
    mainClass.set("MainKt")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("rainpoints") {
                artifactId = "rainpoints"
                from(components["java"])
                pom {
                    developers {
                        developer {
                            name.set("deotime")
                        }
                    }
                }
            }
        }
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
}
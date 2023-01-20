package org.rain.faktorio

import io.ktor.http.HttpMethod
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.scope.APIScope
import org.rain.faktorio.endpoint.Endpoint.Call.Companion.body
import org.rain.faktorio.endpoint.Endpoint.Call.Companion.parameter
import org.rain.faktorio.endpoint.endpoint
import org.rain.faktorio.schemas.property
import java.util.UUID

object TestScopes : APIScope.Library {
    val Glitch = "glitchy" {
        description = "glitchable"
    }
}

data class Test(
    val name: String,
    val amount: Int = 55,
    val alive: Boolean,
    val id: UUID,
    val things: List<String>,
    val type: Type
) {
    enum class Type {
        Glitchy,
        Stackable
    }
}

fun main(): Unit = runBlocking {
    println("ok")
    embeddedServer(Netty, port = 8081) {

        install(Faktorio) {
            scoped { true }
            schemas {
                register {
                    property(Test::amount) {
                        maximum = (100).toBigDecimal()
                    }
                    property(Test::name) {
                        description = "This is the name of the test!"
                        deprecated = true
                    }
                }
            }
            arguments {
                +Endpoint.Argument.Parser<Test> {
                    error("Test is too glitchy to be parsed")
                }
            }
            swagger {
                info {
                    title = "Test api"
                    description = "This is a test api"
                    termsOfService = "Glitching stacks is prohibited"
                }
            }
        }

        routing {
            route("/test/hello/ok") {
                endpoint {
                    description = "some endpoint"
                    method = HttpMethod.Get
                    scope = TestScopes.Glitch
                    call {
                        val name by parameter<String>()
                        val glitchy by parameter<Boolean>()

                        response<Test> {
                            description = "All of your utilities"
                        }

                        body<Test>()

                        request {
                            call.respond(Test("", 23, true, UUID.randomUUID(), emptyList(), Test.Type.Glitchy))
                        }
                    }
                }
            }
        }
    }.start(wait = true)

}
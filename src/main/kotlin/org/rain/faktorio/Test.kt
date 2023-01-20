package org.rain.faktorio

import org.rain.faktorio.model.APIScope
import org.rain.faktorio.model.Endpoint.Call.Companion.body
import org.rain.faktorio.model.Endpoint.Call.Companion.parameter
import org.rain.faktorio.model.endpoint
import org.rain.faktorio.schemas.property
import io.ktor.http.HttpMethod
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
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

fun main() {
}

fun a(): Unit = runBlocking {
    embeddedServer(Netty, port = 8080) {

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

                        body<Test> {
                            description = "The body"
                        }

                        response<Test> {
                            description = "All of your utilities"
                        }

                        request {
                            call.respond("Hello $name, you are ${if (glitchy) "glitchy" else "antiglitchy"}.")
                        }
                    }
                }
            }
        }
    }.start(wait = true)

}
package co.q64.faktorio

import co.q64.faktorio.argument.IntArgumentParser
import co.q64.faktorio.model.APIScope
import co.q64.faktorio.model.endpoint
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.swagger.SwaggerConfig
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking

object TestScopes : APIScope.Library {
    val Glitch = "glitchy" {
        description = "glitchable"
    }
}

fun main(): Unit = runBlocking {

    embeddedServer(Netty, port = 8080) {

        install(Faktorio) {
            scoped { false }
        }
        routing {
            route("/") {
                endpoint {
                    description = "some endpoint"
                    method = HttpMethod.Get
                    scope = TestScopes.Glitch
                    secret = true
                    call {
                        val name by parameter<String>()
                        val glitchy by parameter<Boolean>()

                        response(HttpStatusCode.ExpectationFailed) {
                            description = "Utility"
                            example = Any()
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
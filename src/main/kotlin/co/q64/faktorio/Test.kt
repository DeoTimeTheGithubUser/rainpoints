package co.q64.faktorio

import co.q64.faktorio.model.APIScope
import co.q64.faktorio.model.endpoint
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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
    val id: UUID
)


fun main(): Unit = runBlocking {

    embeddedServer(Netty, port = 8080) {

        install(Faktorio) {
            scoped { false }
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
                        response(HttpStatusCode.ExpectationFailed) {
                            description = "Utility"
                            example = "I love glitchy booleans"
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
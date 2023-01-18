package co.q64.faktorio

import co.q64.faktorio.model.endpoint
import io.ktor.http.HttpMethod
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.method
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking


fun main(): Unit = runBlocking {

    embeddedServer(Netty, port = 8080) {
        routing {
            route("/") {
                endpoint {
                    description = "some endpoint"
                    method = HttpMethod.Get

                    call {
                        val name by parameter<String>()
                        val glitchy by parameter<Boolean>()
                        request {
                            call.respond("Hello $name, you are ${if (glitchy) "glitchy" else "antiglitchy"}.")
                        }
                    }
                }
            }
        }
    }.start(wait = true)

}
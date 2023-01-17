package co.q64.faktorio

import co.q64.faktorio.model.endpoint
import io.ktor.http.HttpMethod
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking


fun main(): Unit = runBlocking {

    embeddedServer(Netty, port = 9123) {
        routing {
            route("/test") {
                endpoint {
                    description = "some endpoint"
                    method = HttpMethod.Post

                    call {
                        val name by parameter<String>()
                        request {
                            call.respond("Hello $name")
                        }
                    }
                }
            }
        }
    }

}
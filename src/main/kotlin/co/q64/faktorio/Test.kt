package co.q64.faktorio

import co.q64.faktorio.model.endpoint
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    embeddedServer(Netty, port = 9123) {
        routing {
            route("/test") {
                endpoint {
                    call {
                        val test by parameter<String>()

                        request {

                        }
                    }
                }
            }
        }
    }
}
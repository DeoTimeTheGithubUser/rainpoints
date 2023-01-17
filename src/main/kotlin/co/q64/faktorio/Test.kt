package co.q64.faktorio

import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = runBlocking {
    embeddedServer(Netty, port = 9123) {
        routing {
            route("/test") {
                endpoint {
                    description = "Some description"
                }
            }
        }
    }
}
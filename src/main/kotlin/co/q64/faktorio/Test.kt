package co.q64.faktorio

import co.q64.faktorio.argument.IntArgumentParser
import co.q64.faktorio.argument.finite
import co.q64.faktorio.model.APIScope
import co.q64.faktorio.model.endpoint
import com.github.victools.jsonschema.generator.MethodScope
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
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
import java.math.BigInteger
import kotlin.reflect.typeOf

object TestScopes : APIScope.Library {
    val Glitch = "glitchy" {
        description = "glitchable"
    }
}

data class Test(
    val name: String,
    val amount: Int = 55,
    val alive: Boolean
)

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
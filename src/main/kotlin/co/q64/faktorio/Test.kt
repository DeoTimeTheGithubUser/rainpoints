package co.q64.faktorio

import co.q64.faktorio.model.APIScope
import co.q64.faktorio.model.endpoint
import co.q64.faktorio.schemas.property
import co.q64.faktorio.schemas.schema
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperties
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.BooleanSchema
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible

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

private val mapper by lazy {
    ObjectMapper().writerWithDefaultPrettyPrinter()
}

fun main() {
    val schema = schema {
        property(Test::name) {
            name = "the name of this test"
        }
    }
    println(mapper.writeValueAsString(schema))
//    val convert = ModelConverters.getInstance()
//    val schema = convert.readAll(Test::class.java)
//    println(mapper.writeValueAsString(schema))
}

fun a(): Unit = runBlocking {
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
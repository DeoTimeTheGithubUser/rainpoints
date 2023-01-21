package org.rain.faktorio

import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.rain.faktorio.argument.max
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.endpoint.Endpoint.Call.Companion.parameter
import org.rain.faktorio.endpoint.endpoint
import org.rain.faktorio.schemas.SchemaConfiguration
import org.rain.faktorio.schemas.property
import org.rain.faktorio.scope.APIScope
import java.util.UUID

object TestScopes : APIScope.Library {
    val Glitch = "glitchy" {
        description = "glitchable"
    }
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = serialDescriptor<String>()

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString("$value")
    }

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}

@Serializable
data class Test(
    val name: String,
    val amount: Int = 55,
    val alive: Boolean,
    val id: @Serializable(with = UUIDSerializer::class) UUID,
    val things: List<String>,
    val type: Type
) {
    enum class Type {
        Glitchy,
        Stackable
    }
}

fun main(): Unit = runBlocking {

    embeddedServer(Netty, port = 8080) {

        install(ContentNegotiation) {
            json()
        }
        install(Faktorio) {
            scoped { true }
            schemas {
                +SchemaConfiguration {
                    property(Test::amount) {
                        maximum = (100).toBigDecimal()
                    }
                    property(Test::name) {
                        description = "This is the name of the test!"
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
                    method = HttpMethod.Post
                    scope = TestScopes.Glitch
                    call {
                        val name by parameter<String>()
                        val glitchy by parameter<Boolean>()
                        val total by parameter<Int>().max(55)

                        execute { body: Test ->
                            delay(555)
                            println("the body be like: $body")
                            return@execute "good job $name, $glitchy, $total"
                        }

                    }
                }
            }
        }
    }.start(wait = true)

}

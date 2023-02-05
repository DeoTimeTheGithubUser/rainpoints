package co.q64.rainpoints.test

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
import co.q64.rainpoints.Rainpoints
import co.q64.rainpoints.argument.max
import co.q64.rainpoints.endpoint.Endpoint
import co.q64.rainpoints.endpoint.Endpoint.Call.Companion.parameter
import co.q64.rainpoints.endpoint.endpoint
import co.q64.rainpoints.schemas.SchemaConfiguration
import co.q64.rainpoints.schemas.property
import co.q64.rainpoints.scope.APIScope
import java.util.UUID

object TestScopes : APIScope.Library by APIScope.Library.Root {
    val Glitch = "glitchy" {
        name = "Stacky"
        description = "Shows your glitchable utilites"
    }

    object Utilities : APIScope by "utilities"() {
        val Stacking = "stacking"()
        val Packing = "ppacking"()
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
    val type: Type,
    val recursiveLink: RecursiveLink
) {
    @Serializable
    class RecursiveLink(val test: Test)
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
        install(Rainpoints) {
            scopes {
                libraries {
                    println("1")
                    +TestScopes
                    println("2")
                }
            }
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
                defaultDescriptionByScope = true
                info {
                    title = "Test api"
                    description = "This is a test api"
                    termsOfService = "Glitching stacks is prohibited"
                }
                oauth {
                    authorizationUrl = "idk"
                }
            }
        }



        try {
            routing {
                route("/test/hello/ok") {
                    endpoint {
                        player = true
                        method = HttpMethod.Post
                        scope = TestScopes.Utilities.Packing
                        call {
                            val name by parameter<String>()
                            val glitchy by parameter<Boolean>()
                            val total by parameter<Int>().max(55)

                            execute { body: Test ->
                                delay(555)
                                println("the body: $body")
                                return@execute "good job $name, $glitchy, $total"
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start(wait = true)

}
package co.q64.faktorio.model

import co.q64.faktorio.FaktorioDsl
import co.q64.faktorio.argument.StringArgumentParser
import co.q64.faktorio.argument.StringArgumentParser.properties
import co.q64.faktorio.argument.standardType
import co.q64.faktorio.internal.ArgumentProcessor
import co.q64.faktorio.internal.scopeHandler
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.createRouteFromPath
import io.ktor.server.routing.method
import io.ktor.util.pipeline.PipelineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias RequestHandler = suspend PipelineContext<*, ApplicationCall>.() -> Unit

class Endpoint(
    private val route: Route,
    var description: String? = null,
    var method: HttpMethod = HttpMethod.Get,
    var secret: Boolean = false,
    var scope: APIScope? = null,
    private var call: (() -> Call)? = null
) {

    val arguments: List<Argument<*>>
        get() = call?.invoke()?.arguments.orEmpty()

    @FaktorioDsl
    fun call(closure: Call.() -> Unit) {
        call = { Call().apply(closure).build() }
    }

    private suspend fun PipelineContext<*, ApplicationCall>.processCall() {
        scope?.let {
            if (!call.scopeHandler(this, it)) {
                if (secret) throw NotFoundException()
                else {
                    call.respond(HttpStatusCode.Unauthorized, "Missing required scope \"${it.name}\".")
                    throw CancellationException()
                }
            }
        }
        val handler = this@Endpoint.call?.invoke() ?: return
        val factory = ArgumentProcessor(call)
        handler.arguments.forEach { factory.processParameter(it) }
        handler.request?.let { it() }
    }

    internal fun build() = apply {
        route.apply {
            method(method) {
                handle {
                    runCatching { processCall() }
                }
            }
        }
    }

    class Call(
        internal val responses: MutableList<Response> = mutableListOf(),
        @PublishedApi internal val arguments: MutableList<Argument<*>> = mutableListOf(),
        internal var request: RequestHandler? = null
    ) {
        fun parameter(
            name: String? = null,
            description: String? = null,
            type: Argument.Type = Argument.Type.QueryParameter,
        ): Argument<String> =
            (Argument(name, type, StringArgumentParser, description))

        @JvmName("reifiedParameter")
        inline fun <reified T> parameter(
            name: String? = null,
            description: String? = null,
            type: Argument.Type = Argument.Type.QueryParameter,
        ) = parameter(name, description, type).typed<T>()

        fun response(code: HttpStatusCode = HttpStatusCode.OK, closure: Response.() -> Unit) {
            responses += Response(code).apply(closure)
        }

        operator fun <T> Argument<T>.provideDelegate(ref: Any?, prop: KProperty<*>) =
            let {
                (name?.let { this } ?: copy(name = prop.name))
            }.also { arguments += it }

        operator fun <T> Argument<T>.getValue(ref: Nothing?, prop: KProperty<*>) =
            value ?: error("Tried to access parameter $name's value outside of a request.")

        @FaktorioDsl
        fun request(closure: RequestHandler) {
            request = closure
        }

        internal fun build() = apply {
            arguments.forEach { with(it.parser) { properties() } }
        }

        data class Response(
            val code: HttpStatusCode,
            var description: String? = null,
            var example: Any? = null
        )

    }

    data class Argument<T> @PublishedApi internal constructor(
        val name: String?,
        val paramType: Type,
        val parser: Parser<T>,
        val description: String? = null,
        val required: Boolean = true,
        internal var value: T? = null,
    ) {

        fun <R> parsed(parser: Parser<R>) =
            cast<R>().copy(parser = parser)

        inline fun <reified R> map(crossinline functor: (T) -> R) =
            parsed(Parser { functor(parser.parse(it)) })

        inline fun <reified R> typed() = cast<R>().copy(parser = standardType())

        fun optional() = cast<T?>().copy(required = false)

        interface Parser<T> {
            val type: KType
            val description: String
            fun parse(input: String): T
            fun Call.properties() = Unit

            companion object {
                data class Simple<T>(
                    override val type: KType,
                    override val description: String,
                    val props: Call.() -> Unit,
                    val parser: (String) -> T,
                ) : Parser<T> {
                    override fun parse(input: String) = parser(input)
                    override fun Call.properties() = props()
                }

                inline operator fun <reified T> invoke(
                    description: String = "${typeOf<T>()} type argument.",
                    noinline props: Call.() -> Unit = {},
                    noinline parse: (String) -> T = { error("Default parse method should be overridden.") }
                ) =
                    Simple(typeOf<T>(), description, props, parse)
            }
        }

        enum class Type {
            Parameter,
            QueryParameter
        }

        @PublishedApi
        @Suppress("UNCHECKED_CAST")
        internal fun <R> cast() = (this as Argument<R>)
    }
}

@FaktorioDsl
fun Route.endpoint(path: String? = null, closure: Endpoint.() -> Unit) {
    val route = (path?.let { createRouteFromPath(it) } ?: this)
    Endpoint(route).apply(closure).build()
}
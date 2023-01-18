package co.q64.faktorio.model

import co.q64.faktorio.FaktorioDsl
import co.q64.faktorio.argument.StringArgumentParser
import co.q64.faktorio.argument.typedArgument
import co.q64.faktorio.internal.ArgumentProcessor
import co.q64.faktorio.internal.scopeHandler
import co.q64.faktorio.util.Buildable
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
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias RequestHandler = suspend PipelineContext<*, ApplicationCall>.() -> Unit

class Endpoint(
    private val route: Route,
    var summary: String? = null,
    var description: String? = null,
    var method: HttpMethod = HttpMethod.Get,
    var secret: Boolean = false,
    var scope: APIScope? = null,
    private var call: (() -> Call)? = null
) : Buildable<Operation> {

    val arguments: List<Argument<*>>
        get() = call?.invoke()?.arguments.orEmpty()

    @FaktorioDsl
    fun call(closure: Call.() -> Unit) {
        call = { Call().apply(closure).apply() }
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

    internal fun apply() = apply {
        route.apply {
            method(method) {
                handle {
                    runCatching { processCall() }
                }
            }
        }
    }

    override fun build() = Operation().also { op ->
        op.summary(summary)
            .description(description)

        call?.invoke()?.let { call ->
            op.parameters(call.arguments.map { it.build() })
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
            type: Argument.Type = Argument.Type.Query,
        ): Argument<String> =
            (Argument(name, type, StringArgumentParser, description))

        @JvmName("reifiedParameter")
        inline fun <reified T> parameter(
            name: String? = null,
            description: String? = null,
            type: Argument.Type = Argument.Type.Query,
            parser: Argument.Parser<T> = typedArgument()
        ) = parameter(name, description, type).parsed(parser)

        @FaktorioDsl
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

        internal fun apply() = apply {
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
        val example: T? = null,
        internal var value: T? = null,
    ) : Buildable<Parameter> {

        fun <R> parsed(parser: Parser<R>) =
            cast<R>().copy(parser = parser)

        fun chain(props: Call.() -> Unit = {}, closure: (T) -> Unit) =
            parsed(Parser(parser.type, props = props) { parser.parse(it).also(closure) })

        inline fun <reified R> map(crossinline functor: (T) -> R) =
            parsed(Parser { functor(parser.parse(it)) })

        fun optional() = cast<T?>().copy(required = false)

        fun example(example: T) = copy(example = example)

        interface Parser<T> {
            val type: KType
            fun parse(input: String): T
            val description: String get() = "$type type argument."
            fun Call.properties() = Unit

            data class Simple<T>(
                override val type: KType,
                override val description: String,
                val props: Call.() -> Unit,
                val parser: (String) -> T,
            ) : Parser<T> {
                override fun parse(input: String) = parser(input)
                override fun Call.properties() = props()
            }

            companion object {

                operator fun <T> invoke(
                    type: KType,
                    description: String = "$type type argument.",
                    props: Call.() -> Unit = {},
                    parse: (String) -> T
                ) = Simple(type, description, props, parse)

                inline operator fun <reified T> invoke(
                    description: String = "${typeOf<T>()} type argument.",
                    noinline props: Call.() -> Unit = {},
                    noinline parse: (String) -> T
                ) =
                    Simple(typeOf<T>(), description, props, parse)
            }
        }

        enum class Type {
            Path,
            Query,
            Header,
            Cookie
        }

        override fun build() = Parameter().also { param ->
            param
                .name(name)
                .description(description)
                .`in`(paramType.name.lowercase())
                .required(required)
                .example(example)
        }

        @PublishedApi
        @Suppress("UNCHECKED_CAST")
        internal fun <R> cast() = (this as Argument<R>)
    }
}

@FaktorioDsl
fun Route.endpoint(path: String? = null, closure: Endpoint.() -> Unit) {
    val route = (path?.let { createRouteFromPath(it) } ?: this)
    Endpoint(route).apply(closure).apply()
}
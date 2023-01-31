package co.q64.rainpoints.endpoint

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.createRouteFromPath
import io.ktor.util.pipeline.PipelineContext
import co.q64.rainpoints.RainpointsDsl
import co.q64.rainpoints.argument.argumentParser
import co.q64.rainpoints.impl.RainEndpoint
import co.q64.rainpoints.scope.APIScope
import co.q64.rainpoints.util.ApplicationContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias ExecutionHandler<B, R> = suspend PipelineContext<Unit, ApplicationCall>.(B) -> R

interface Endpoint : ApplicationContext {
    var summary: String?
    var description: String?
    var method: HttpMethod
    var secret: Boolean
    var scope: APIScope?
    var category: String?
    val path: String
    val arguments: List<Argument<*>>

    @RainpointsDsl
    fun call(closure: Call.() -> Unit)

    @RainpointsDsl
    fun <R> execute(handler: ExecutionHandler<Nothing, R>) = execute(null, handler)
    @RainpointsDsl
    fun <B, R> execute(overload: Nothing? = null, handler: ExecutionHandler<B, R>)

    interface Call : ApplicationContext {

        @RainpointsDsl
        fun <R> execute(handler: ExecutionHandler<Nothing, R>) = execute(null, handler)
        @RainpointsDsl
        fun <B, R> execute(overload: Nothing? = null, handler: ExecutionHandler<B, R>)


        operator fun <T> Argument<T>.provideDelegate(ref: Nothing?, prop: KProperty<*>): Argument<T>
        operator fun <T> Argument<T>.getValue(ref: Nothing?, prop: KProperty<*>): T

        interface Request<T : Any> {
            var description: String?
            var required: Boolean
        }

        interface Response<T : Any> {
            val code: HttpStatusCode
            var description: String?
        }

        @Suppress("ClassName")
        interface _Internal : Call {
            fun <T : Any> request(
                type: KClass<T>,
                closure: Request<T>.() -> Unit = {}
            )

            fun <T : Any> response(
                code: HttpStatusCode = HttpStatusCode.OK,
                type: KClass<T>? = null,
                closure: Response<T>.() -> Unit = {}
            )

            fun parameter(
                name: String? = null,
                description: String? = null,
                type: Argument.Type = Argument.Type.Query,
            ): Argument<String>

        }

        companion object {

            @RainpointsDsl
            @JvmName("reifiedParameter")
            inline fun <reified T> Call.parameter(
                name: String? = null,
                description: String? = null,
                type: Argument.Type = Argument.Type.Query,
                parser: Argument.Parser<T> = application.argumentParser()
            ) = (this as _Internal).parameter(name, description, type).parsed(parser)

            @RainpointsDsl
            inline fun <reified T : Any> Call.response(
                code: HttpStatusCode = HttpStatusCode.OK,
                noinline closure: Response<T>.() -> Unit = {}
            ) = (this as _Internal).response(code, T::class, closure)

            @RainpointsDsl
            @JvmName("statusResponse")
            fun Call.response(
                code: HttpStatusCode = HttpStatusCode.OK,
                closure: Response<Nothing>.() -> Unit = {}
            ) = (this as _Internal).response(code, closure = closure)

            @RainpointsDsl
            inline fun <reified T : Any> Call.request(
                noinline closure: Request<T>.() -> Unit = {}
            ) = (this as _Internal).request(T::class, closure)
        }
    }

    interface Argument<T> : ApplicationContext {
        val name: String?
        val paramType: Type
        val parser: Parser<T>
        val description: String?
        val required: Boolean
        val example: T?

        fun <R> parsed(parser: Parser<R>): Argument<R>
        fun chain(props: Call.() -> Unit = {}, closure: suspend ApplicationCall.(T) -> Unit): Argument<T>
        fun require(happens: String = "requirement is not met", check: (T) -> Boolean): Argument<T>
        fun optional(): Argument<T?>
        fun example(example: T): Argument<T>

        companion object {
            inline fun <T, reified R> Argument<T>.map(crossinline functor: suspend ApplicationCall.(T) -> R) =
                parsed(Parser {
                    with(parser) { functor(parse(it)) }
                })
        }

        interface Parser<T> {
            val type: KType
            suspend fun ApplicationCall.parse(input: String): T
            val description: String get() = "$type type argument."
            fun Call.properties() = Unit

            data class Simple<T>(
                override val type: KType,
                override val description: String,
                val props: Call.() -> Unit,
                val parser: suspend ApplicationCall.(String) -> T,
            ) : Parser<T> {
                override suspend fun ApplicationCall.parse(input: String) = parser(input)
                override fun Call.properties() = props()
            }

            companion object {

                operator fun <T> invoke(
                    type: KType,
                    description: String = "$type type argument.",
                    props: Call.() -> Unit = {},
                    parse: suspend ApplicationCall.(String) -> T
                ) = Simple(type, description, props, parse)

                inline operator fun <reified T> invoke(
                    description: String = "${typeOf<T>()} type argument.",
                    noinline props: Call.() -> Unit = {},
                    noinline parse: suspend ApplicationCall.(String) -> T
                ) =
                    Simple(typeOf<T>(), description, props, parse)

                inline fun <reified T> simple(
                    description: String = "${typeOf<T>()} type argument.",
                    noinline props: Call.() -> Unit = {},
                    noinline parse: (String) -> T
                ) =
                    Simple(typeOf<T>(), description, props) { parse(it) }
            }
        }

        enum class Type {
            Path,
            Query,
            Header,
            Cookie
        }
    }
}

@RainpointsDsl
fun Route.endpoint(path: String? = null, closure: Endpoint.() -> Unit) {
    val route = (path?.let { createRouteFromPath(it) } ?: this)
    application.endpoints += RainEndpoint(route).apply(closure).configure()
}
package org.rain.faktorio.endpoint

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.createRouteFromPath
import io.ktor.util.pipeline.PipelineContext
import org.rain.faktorio.FaktorioDsl
import org.rain.faktorio.argument.argumentParser
import org.rain.faktorio.impl.RainEndpoint
import org.rain.faktorio.scope.APIScope
import org.rain.faktorio.util.ApplicationContext
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.reflect.KSuspendFunction1

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

    @FaktorioDsl
    fun call(closure: Call.() -> Unit)

    interface Call : ApplicationContext {

        @FaktorioDsl
        fun <R> execute(handler: ExecutionHandler<Nothing, R>) = execute(null, handler)
        @FaktorioDsl
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

            @FaktorioDsl
            @JvmName("reifiedParameter")
            inline fun <reified T> Call.parameter(
                name: String? = null,
                description: String? = null,
                type: Argument.Type = Argument.Type.Query,
                parser: Argument.Parser<T> = application.argumentParser()
            ) = (this as _Internal).parameter(name, description, type).parsed(parser)

            @FaktorioDsl
            inline fun <reified T : Any> Call.response(
                code: HttpStatusCode = HttpStatusCode.OK,
                noinline closure: Response<T>.() -> Unit = {}
            ) = (this as _Internal).response(code, T::class, closure)

            @FaktorioDsl
            @JvmName("statusResponse")
            fun Call.response(
                code: HttpStatusCode = HttpStatusCode.OK,
                closure: Response<Nothing>.() -> Unit = {}
            ) = (this as _Internal).response(code, closure = closure)

            @FaktorioDsl
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
        fun chain(props: Call.() -> Unit = {}, closure: (T) -> Unit): Argument<T>
        fun require(happens: String = "requirement is not met", check: (T) -> Boolean): Argument<T>
        fun optional(): Argument<T?>
        fun example(example: T): Argument<T>

        companion object {
            inline fun <T, reified R> Argument<T>.map(crossinline functor: (T) -> R) =
                parsed(Parser { functor(parser.parse(it)) })
        }

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
    }
}

@FaktorioDsl
fun Route.endpoint(path: String? = null, closure: Endpoint.() -> Unit) {
    val route = (path?.let { createRouteFromPath(it) } ?: this)
    application.endpoints += RainEndpoint(route).apply(closure).configure()
}
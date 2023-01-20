package org.rain.faktorio.model

import org.rain.faktorio.FaktorioDsl
import org.rain.faktorio.argument.typedArgument
import org.rain.faktorio.impl.RainEndpoint
import org.rain.faktorio.internal.endpoints
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.createRouteFromPath
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias RequestHandler = suspend PipelineContext<*, ApplicationCall>.() -> Unit

interface Endpoint {
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

    interface Call {
        fun parameter(
            name: String? = null,
            description: String? = null,
            type: Argument.Type = Argument.Type.Query,
        ): Argument<String>

        @FaktorioDsl
        fun request(closure: RequestHandler)


        @FaktorioDsl
        fun <T : Any> response(
            code: HttpStatusCode = HttpStatusCode.OK,
            type: KClass<T>? = null,
            closure: Response<T>.() -> Unit = {}
        )

        @FaktorioDsl
        fun response(
            code: HttpStatusCode = HttpStatusCode.OK,
            closure: Response<Nothing>.() -> Unit = {}
        ) = response<Nothing>(code, closure = closure)

        @FaktorioDsl
        fun <T : Any> body(
            type: KClass<T>,
            closure: Body<T>.() -> Unit = {}
        )

        operator fun <T> Argument<T>.provideDelegate(ref: Nothing?, prop: KProperty<*>): Argument<T>
        operator fun <T> Argument<T>.getValue(ref: Nothing?, prop: KProperty<*>): T

        interface Body<T : Any> {
            var description: String?
            var required: Boolean
        }

        interface Response<T : Any> {
            val code: HttpStatusCode
            var description: String?
        }

        companion object {
            @JvmName("reifiedParameter")
            inline fun <reified T> Call.parameter(
                name: String? = null,
                description: String? = null,
                type: Argument.Type = Argument.Type.Query,
                parser: Argument.Parser<T> = typedArgument()
            ) = parameter(name, description, type).parsed(parser)

            @FaktorioDsl
            inline fun <reified T : Any> Call.response(
                code: HttpStatusCode = HttpStatusCode.OK,
                noinline closure: Response<T>.() -> Unit
            ) = response(code, T::class, closure)

            @FaktorioDsl
            inline fun <reified T : Any> Call.body(
                noinline closure: Body<T>.() -> Unit = {}
            ) = body(T::class, closure)
        }
    }

    interface Argument<T> {
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
            inline fun <T, reified R> Endpoint.Argument<T>.map(crossinline functor: (T) -> R) =
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
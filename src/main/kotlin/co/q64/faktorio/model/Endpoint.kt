package co.q64.faktorio.model

import io.ktor.http.HttpMethod
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.server.routing.createRouteFromPath
import io.ktor.server.routing.method
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias RequestHandler = suspend PipelineContext<*, ApplicationCall>.() -> Unit

class Endpoint(
    private val route: Route,
    var description: String? = null,
    var method: HttpMethod = HttpMethod.Get,
    var scope: APIScope? = null,
    private var call: (() -> Call)? = null
) {

    fun call(closure: Call.() -> Unit) {
        call = { Call().apply(closure) }
    }

    private fun PipelineContext<*, ApplicationCall>.processCall() {
        val call = this@Endpoint.call?.invoke()

    }

    fun build() {

    }

    class Call(
        @PublishedApi internal val parameters: MutableList<Parameter<*>> = mutableListOf(),
        internal var request: RequestHandler? = null
    ) {
        inline fun <reified T : Any> parameter(
            name: String? = null,
            type: Parameter.Type = Parameter.Type.QueryParameter,
            description: String? = null
        ): Parameter<T> =
            (Parameter(name, description, typeOf<T>(), type) { this as? T }).also { parameters += it }

        fun request(closure: RequestHandler) {
            request = closure
        }

    }

    data class Parameter<T> @PublishedApi internal constructor(
        var name: String?,
        val description: String? = null,
        val type: KType,
        val paramType: Type,
        internal var value: T? = null,
        val cast: Any.() -> T?, // no unchecked cast!
    ) {

        operator fun provideDelegate(ref: Any?, prop: KProperty<*>) =
            this.also { name ?: run { name = prop.name } }


        operator fun getValue(ref: Nothing?, prop: KProperty<*>) =
            value ?: error("Tried to access parameter $name's value outside of a request.")


    enum class Type {
        Parameter,
        QueryParameter
    }
}
}

fun Route.endpoint(path: String? = null, closure: Endpoint.() -> Unit) {
    val route = (path?.let { createRouteFromPath(it) } ?: this)
    Endpoint(route).apply(closure).build()
}
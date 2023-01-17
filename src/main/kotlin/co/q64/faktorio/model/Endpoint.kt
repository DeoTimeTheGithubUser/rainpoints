package co.q64.faktorio.model

import io.ktor.http.HttpMethod
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.routing.Route
import io.ktor.server.routing.createRouteFromPath
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias Request = PipelineContext<*, ApplicationCall>

class Endpoint(
    private val route: Route,
    var description: String? = null,
    var method: HttpMethod = HttpMethod.Get,
    var scope: APIScope? = null,
    private var call: Call? = null
) {

    fun call(closure: Call.() -> Unit) {

    }

    fun build() {
        TODO()
    }

    class Call(
        @PublishedApi internal val parameters: MutableList<Parameter<*>> = mutableListOf(),
        private var request: (Request.() -> Unit)? = null
    ) {
        inline fun <reified T : Any> parameter(
            name: String? = null,
            type: Parameter.Type = Parameter.Type.QueryParameter,
            description: String? = null
        ) =
            Parameter<T>(typeOf<T>(), type, name, description).also { parameters += it }

        fun request(closure: Request.() -> Unit) {
            request = closure
        }

    }

    data class Parameter<T> @PublishedApi internal constructor(
        val type: KType,
        val paramType: Type,
        val name: String? = null,
        val description: String? = null
    ) {

        operator fun provideDelegate(ref: Any?, prop: KProperty<*>) =
            name?.let { this } ?: copy(name = prop.name)


        // todo please make this cleaner
        context(PipelineContext<*, ApplicationCall>) @Suppress("UNCHECKED_CAST")
        operator fun getValue(ref: Any?, prop: KProperty<*>): T =
            ((if (paramType == Type.QueryParameter) call.request.queryParameters[name!!]
            else call.parameters[name!!])?.let {
                Json.decodeFromString(serializer(type), it) // todo cache this with the call somehow
            } as T?) ?: throw MissingRequestParameterException(name)

        operator fun getValue(ref: Any?, prop: KProperty<*>): T =
            error("Value of parameter $name can only be accessed within a request.")

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
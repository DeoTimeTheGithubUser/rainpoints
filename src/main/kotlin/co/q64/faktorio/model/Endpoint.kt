package co.q64.faktorio.model

import io.ktor.http.HttpMethod
import io.ktor.server.routing.Route
import io.ktor.server.routing.createRouteFromPath
import kotlin.reflect.KProperty
import kotlin.reflect.KType

class Endpoint(
    private val route: Route,
    var description: String? = null,
    var method: HttpMethod = HttpMethod.Get,
    var scope: APIScope? = null,
    var call: Call? = null
) {
    fun build() {
        TODO()
    }

    class Call(
        private val parameters: MutableList<Parameter<*>> = mutableListOf()
    ) {

    }

    data class Parameter<T>(
        val type: KType,
        val paramType: Type,
        val name: String? = null,
        val description: String? = null
    ) {

        operator fun provideDelegate(ref: Any?, prop: KProperty<*>) =
            name?.let { this } ?: copy(name = prop.name)

//        context(Request) // todo implement
        operator fun getValue(ref: Any?, prop: KProperty<*>): T = TODO()

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
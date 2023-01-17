package co.q64.faktorio.model

import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.MissingRequestParameterException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

internal class ArgumentFactory(
    val call: ApplicationCall
) {
    fun <T> buildArgument(parameter: Endpoint.Parameter<T>): T {
        val name = parameter.name ?: error("Parameter still has no name.")
        val raw =
            (if (parameter.paramType == Endpoint.Parameter.Type.QueryParameter) call.request.queryParameters[name]
            else call.parameters[name]) ?: throw MissingRequestParameterException(name)
        return Json.decodeFromString(serializer(parameter.type), raw)?.let(parameter.cast)
            ?: error("Deserialization failed on arguemnt $name")
    }

    fun <T> processParameter(parameter: Endpoint.Parameter<T>) {
        parameter.apply { value = buildArgument(this) }
    }
}
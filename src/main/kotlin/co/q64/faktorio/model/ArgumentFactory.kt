package co.q64.faktorio.model

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.coroutines.cancellation.CancellationException

internal class ArgumentFactory(
    val call: ApplicationCall
) {
    suspend fun <T> buildArgument(parameter: Endpoint.Parameter<T>): T? {
        val name = parameter.name ?: run {
            call.respond(HttpStatusCode.InternalServerError)
            error("Parameter $parameter still has no name.")
        }

        val raw =
            (if (parameter.paramType == Endpoint.Parameter.Type.QueryParameter) call.request.queryParameters[name]
            else call.parameters[name])
                ?: (if (parameter.optional) return null
                else throw MissingRequestParameterException(name))

        return runCatching {
            Json.decodeFromString(serializer(parameter.type), raw)
        }.getOrNull()?.let(parameter.cast) ?: run {
            call.respond(HttpStatusCode.BadRequest, "Could not parse parameter \"$name\"")
            throw CancellationException()
        }
    }

    suspend fun <T> processParameter(parameter: Endpoint.Parameter<T>) {
        parameter.apply { value = buildArgument(this) }
    }

}
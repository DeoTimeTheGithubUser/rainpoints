package co.q64.faktorio.internal

import co.q64.faktorio.model.Endpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.response.respond

internal class ArgumentProcessor(
    private val call: ApplicationCall
) {
    suspend fun <T> buildArgument(argument: Endpoint.Argument<T>): T? {
        val name = argument.name ?: run {
            call.respond(HttpStatusCode.InternalServerError)
            error("Parameter $argument still has no name.")
        }

        val raw =
            (if (argument.paramType == Endpoint.Argument.Type.QueryParameter) call.request.queryParameters[name]
            else call.parameters[name])
                ?: (if (!argument.required) return null
                else throw MissingRequestParameterException(name))

        return runCatching {
            argument.parser.parse(raw)
        }.getOrElse { throw BadRequestException("Could not parse parameter \"$name\" to type ${argument.parser.type}: ${it.message}") }
    }

    suspend fun <T> processParameter(argument: Endpoint.Argument<T>) {
        argument.apply { value = buildArgument(this) }
    }

}
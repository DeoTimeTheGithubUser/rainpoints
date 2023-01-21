package org.rain.faktorio.argument

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.response.respond
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.impl.RainArgument

internal class ArgumentProcessor(
    private val call: ApplicationCall
) {
    private suspend fun <T> buildArgument(argument: Endpoint.Argument<T>): T? {
        val name = argument.name ?: run {
            call.respond(HttpStatusCode.InternalServerError)
            error("Parameter $argument still has no name.")
        }

        val raw = when (argument.paramType) {
            Endpoint.Argument.Type.Query -> call.request.queryParameters[name]
            Endpoint.Argument.Type.Path -> call.parameters[name]
            Endpoint.Argument.Type.Header -> call.request.headers[name]
            Endpoint.Argument.Type.Cookie -> call.request.cookies[name]
        } ?: (if (!argument.required) return null
        else throw MissingRequestParameterException(name))

        return runCatching {
            argument.parser.parse(raw)
        }.getOrElse { throw BadRequestException("Could not parse parameter \"$name\" to type ${argument.parser.type}: ${it.message}") }
    }

    suspend fun <T> processParameter(argument: RainArgument<T>) {
        argument.apply { value = buildArgument(this) }
    }

}
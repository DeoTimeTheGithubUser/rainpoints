package co.q64.rainpoints.argument

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.response.respond
import co.q64.rainpoints.endpoint.Endpoint
import co.q64.rainpoints.impl.RainArgument
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext

internal class ArgumentProcessor(
    private val context: PipelineContext<Unit, ApplicationCall>
) {
    private suspend fun <T> buildArgument(argument: Endpoint.Argument<T>): T? {
        val name = argument.name ?: run {
            context.call.respond(HttpStatusCode.InternalServerError)
            error("Parameter $argument still has no name.")
        }

        val req = context.call.request
        val raw = when (argument.paramType) {
            Endpoint.Argument.Type.Query -> req.queryParameters[name]
            Endpoint.Argument.Type.Path -> context.call.parameters[name]
            Endpoint.Argument.Type.Header -> req.headers[name]
            Endpoint.Argument.Type.Cookie -> req.cookies[name]
        } ?: (if (!argument.required) return null
        else throw MissingRequestParameterException(name))

        return runCatching {
            with(argument.parser) { context.parse(raw) }
        }.getOrElse { throw BadRequestException("Could not parse parameter \"$name\" to type ${argument.parser.type}: ${it.message}") }
    }

    suspend fun <T> processParameter(argument: RainArgument<T>) {
        argument.apply { value = buildArgument(this) }
    }

}
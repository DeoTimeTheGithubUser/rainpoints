package co.q64.rainpoints.impl

import co.q64.rainpoints.argument.StandardArguments
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.BadRequestException
import io.swagger.v3.oas.models.parameters.Parameter
import co.q64.rainpoints.endpoint.Endpoint
import co.q64.rainpoints.endpoint.Endpoint.Call.Companion.response
import co.q64.rainpoints.schemas.registeredSchema
import co.q64.rainpoints.util.Buildable
import co.q64.rainpoints.util.clazz
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.KClass

data class RainArgument<T> @PublishedApi internal constructor(
    override val application: Application,
    override val name: String?,
    override val paramType: Endpoint.Argument.Type,
    override val parser: Endpoint.Argument.Parser<T>,
    override val description: String? = null,
    override val required: Boolean = true,
    override val example: T? = null,
    internal var value: T? = null,
) : Endpoint.Argument<T>, Buildable<Parameter> {

    override fun <R> parsed(parser: Endpoint.Argument.Parser<R>) =
        cast<R>().copy(parser = parser)

    override fun chain(props: Endpoint.Call.() -> Unit, closure: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit) =
        parsed(Endpoint.Argument.Parser(parser.type, props = props) {
            with(parser) { parse(it).also { closure(this@Parser, it) } }
        })

    override fun require(happens: String, check: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Boolean) =
        chain({
            response(HttpStatusCode.BadRequest) {
                description = "When ${happens.replaceFirstChar { it.lowercase() }} for parameter $name."
            }
        }) {
            if (!check(it)) throw BadRequestException("$happens for parameter $name.")
        }

    override fun optional() = cast<T?>().copy(required = false)

    override fun example(example: T) = copy(example = example)

    override fun build(context: Application) = Parameter().also { param ->
        param
            .name(name)
            .description(description)
            .`in`(paramType.name.lowercase())
            .required(required)
            .example(example)
        if (parser.type in StandardArguments)
            param.schema(context.registeredSchema(parser.type.clazz))
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <R> cast() = (this as RainArgument<R>)
}
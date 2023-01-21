package org.rain.faktorio.impl

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.BadRequestException
import io.swagger.v3.oas.models.parameters.Parameter
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.endpoint.Endpoint.Call.Companion.response
import org.rain.faktorio.schemas.registeredSchema
import org.rain.faktorio.util.Buildable
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

    override fun chain(props: Endpoint.Call.() -> Unit, closure: (T) -> Unit) =
        parsed(Endpoint.Argument.Parser(parser.type, props = props) { parser.parse(it).also(closure) })

    override fun require(happens: String, check: (T) -> Boolean) =
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
        (parser.type.classifier as? KClass<*>)?.let { param.schema(context.registeredSchema(it)) }
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun <R> cast() = (this as RainArgument<R>)
}
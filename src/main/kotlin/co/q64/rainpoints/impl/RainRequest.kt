package co.q64.rainpoints.impl

import co.q64.rainpoints.endpoint.Endpoint
import co.q64.rainpoints.util.Buildable
import io.ktor.server.application.Application
import io.swagger.v3.oas.models.parameters.RequestBody
import co.q64.rainpoints.schemas.schemaContent
import kotlin.reflect.KClass

data class RainRequest<T : Any>(
    override var description: String? = null,
    override var required: Boolean = true,
    private val type: KClass<T>? = null
) : Endpoint.Call.Request<T>, Buildable<RequestBody> {
    override fun build(context: Application) = RequestBody().also { req ->
        req.description = description
        req.required = required
        type?.let { req.content(context.schemaContent(it)) }
    }
}
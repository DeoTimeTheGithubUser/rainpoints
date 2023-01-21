package org.rain.rainpoints.impl

import org.rain.rainpoints.endpoint.Endpoint
import org.rain.rainpoints.util.Buildable
import io.ktor.server.application.Application
import io.swagger.v3.oas.models.parameters.RequestBody
import org.rain.rainpoints.schemas.schemaContent
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
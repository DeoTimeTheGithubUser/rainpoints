package org.rain.faktorio.impl

import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.schemas.SchemaFactory.schemaContent
import org.rain.faktorio.util.Buildable
import io.ktor.server.application.Application
import io.swagger.v3.oas.models.parameters.RequestBody
import kotlin.reflect.KClass

data class RainBody<T : Any>(
    override var description: String? = null,
    override var required: Boolean = true,
    private val type: KClass<T>? = null
) : Endpoint.Call.Body<T>, Buildable<RequestBody> {
    override fun build(context: Application) = RequestBody().also { req ->
        req.description = description
        req.required = required
        type?.let { req.content(context.schemaContent(it)) }
    }
}
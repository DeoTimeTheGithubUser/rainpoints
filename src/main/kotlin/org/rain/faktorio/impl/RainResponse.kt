package org.rain.faktorio.impl

import org.rain.faktorio.model.Endpoint
import org.rain.faktorio.schemas.SchemaFactory.schemaContent
import org.rain.faktorio.util.Buildable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.swagger.v3.oas.models.responses.ApiResponse
import kotlin.reflect.KClass

data class RainResponse<T : Any>(
    override val code: HttpStatusCode,
    override var description: String? = null,
    private val type: KClass<T>? = null
) : Endpoint.Call.Response<T>, Buildable<ApiResponse> {
    override fun build(context: Application) = ApiResponse().also { response ->
        response.description = description
        type?.let { response.content(context.schemaContent(it)) }
    }
}
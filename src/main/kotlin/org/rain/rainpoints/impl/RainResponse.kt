package org.rain.rainpoints.impl

import org.rain.rainpoints.endpoint.Endpoint
import org.rain.rainpoints.util.Buildable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.swagger.v3.oas.models.responses.ApiResponse
import org.rain.rainpoints.schemas.schemaContent
import kotlin.reflect.KClass

data class RainResponse<T : Any>(
    override val code: HttpStatusCode,
    override var description: String? = null,
    internal val type: KClass<T>? = null
) : Endpoint.Call.Response<T>, Buildable<ApiResponse> {
    override fun build(context: Application) = ApiResponse().also { response ->
        response.description = description
        type?.let { response.content(context.schemaContent(it)) }
    }
}
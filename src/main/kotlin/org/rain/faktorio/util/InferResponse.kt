package org.rain.faktorio.util

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.rain.faktorio.FaktorioDsl
import org.rain.faktorio.FaktorioExperimental
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.endpoint.Endpoint.Call.Companion.response
import io.ktor.server.response.respond as _respond

@FaktorioExperimental
class InferResponse<T> {
    companion object
}

context (InferResponse<T>)
        @OptIn(FaktorioExperimental::class)
        @FaktorioExperimental
        suspend inline fun <reified T : Any> ApplicationCall.respond(message: T) = _respond(message)

@FaktorioDsl
@FaktorioExperimental
inline fun <reified T : Any> Endpoint.Call.request(
    infer: InferResponse.Companion,
    crossinline closure: suspend context(PipelineContext<*, ApplicationCall>) InferResponse<T>.() -> Unit
) {
    request { closure(InferResponse()) }
    response<T>()
}
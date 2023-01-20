package org.rain.faktorio.impl

import org.rain.faktorio.argument.ArgumentProcessor
import org.rain.faktorio.scope.scopeHandler
import org.rain.faktorio.scope.APIScope
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.util.Buildable
import org.rain.faktorio.util.path
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.method
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement

class RainEndpoint(
    private val route: Route,
    override val application: Application = route.application,
    override var summary: String? = null,
    override var description: String? = null,
    override var method: HttpMethod = HttpMethod.Get,
    override var secret: Boolean = false,
    override var scope: APIScope? = null,
    override var category: String? = null,
    private var call: (() -> RainCall)? = null
) : Endpoint, Buildable<Operation> {

    override val arguments get() = call?.invoke()?.arguments.orEmpty()
    override val path get() = route.path

    override fun call(closure: Endpoint.Call.() -> Unit) {
        call = { RainCall(application).apply(closure).configure() }
    }

    private suspend fun PipelineContext<*, ApplicationCall>.processCall() {
        scope?.let {
            if (!call.scopeHandler(this, it)) {
                if (secret) throw NotFoundException()
                else return call.respond(HttpStatusCode.Unauthorized)
            }
        }
        val handler = this@RainEndpoint.call?.invoke() ?: return
        val processor = ArgumentProcessor(call)
        handler.arguments.forEach { processor.processParameter(it) }
        handler.request?.let { it() }
    }

    internal fun configure() = apply {
        route.apply {
            method(method) {
                handle {
                    processCall()
                }
            }
        }
    }

    override fun build(context: Application) = Operation().also { operation ->
        operation.addTagsItem(category)
        operation
            .summary(summary)
            .description(description)

        call?.invoke()?.let {
            operation.responses(ApiResponses().apply {
                it.responses.forEach { res ->
                    addApiResponse("${res.code.value}", res.build(context))
                }
            })
            operation.requestBody(it.body?.build(context))
        }
        scope?.let { operation.addSecurityItem(SecurityRequirement().addList(it.id)) }
        operation.parameters(arguments.map { it.build(context) })
    }


}
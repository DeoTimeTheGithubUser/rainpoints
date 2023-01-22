package org.rain.rainpoints.impl

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.method
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.rain.rainpoints.argument.ArgumentProcessor
import org.rain.rainpoints.endpoint.Endpoint
import org.rain.rainpoints.endpoint.ExecutionHandler
import org.rain.rainpoints.scope.APIScope
import org.rain.rainpoints.scope.scopeHandler
import org.rain.rainpoints.util.Buildable
import org.rain.rainpoints.util.path
import org.rain.rainpoints.util.typeInfo

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

    override fun <B, R> execute(overload: Nothing?, handler: ExecutionHandler<B, R>) {
        call {
            execute(handler)
        }
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.processCall() {
        scope?.let {
            if (!call.scopeHandler(this, it)) {
                if (secret) throw NotFoundException()
                else return call.respond(HttpStatusCode.Unauthorized)
            }
        }

        val handler = this@RainEndpoint.call?.invoke() ?: return
        val body = handler.bodyType?.let { call.receive<Any>(it.typeInfo) }
        val processor = ArgumentProcessor(call)
        handler.arguments.forEach { processor.processParameter(it) }
        handler.execute?.let {
            @Suppress("UNCHECKED_CAST")
            (it as ExecutionHandler<Any?, Any?>)(this, body)?.let { res ->
                handler.resType?.let { resType ->
                    call.respond(res, resType.typeInfo)
                }
            }
        }
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
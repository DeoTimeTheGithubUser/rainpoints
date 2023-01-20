package co.q64.faktorio

import co.q64.faktorio.internal.scopeHandler
import co.q64.faktorio.model.APIScope
import co.q64.faktorio.schemas.SchemaRegistryKey
import co.q64.faktorio.swagger.SwaggerRoute
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.util.pipeline.PipelineContext

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

val Faktorio = createApplicationPlugin("Faktorio", ::FaktorioConfig) {
    application.attributes.put(SchemaRegistryKey, pluginConfig.registeredSchemas)
    onCall { call ->
        call.scopeHandler = pluginConfig.scopeHandler
    }
    on(MonitoringEvent(ApplicationStarted)) {
        SwaggerRoute(pluginConfig).route(it)
    }
}



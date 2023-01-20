package org.rain.faktorio

import org.rain.faktorio.internal.scopeHandler
import org.rain.faktorio.model.APIScope
import org.rain.faktorio.schemas.SchemaRegistryKey
import org.rain.faktorio.swagger.SwaggerRoute
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.util.pipeline.PipelineContext

@DslMarker
annotation class FaktorioDsl

@RequiresOptIn
annotation class FaktorioExperimental

val Faktorio = createApplicationPlugin("Faktorio", ::FaktorioConfig) {
    application.attributes.put(SchemaRegistryKey, pluginConfig.registeredSchemas)
    onCall { call ->
        call.scopeHandler = pluginConfig.scopeHandler
    }
    on(MonitoringEvent(ApplicationStarted)) {
        SwaggerRoute(pluginConfig).route(it)
    }
}



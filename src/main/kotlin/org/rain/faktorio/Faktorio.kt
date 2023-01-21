package org.rain.faktorio

import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import org.rain.faktorio.argument.ArgumentRegistryKey
import org.rain.faktorio.schemas.SchemaRegistryKey
import org.rain.faktorio.scope.scopeHandler
import org.rain.faktorio.swagger.SwaggerRoute

@DslMarker
annotation class FaktorioDsl

@RequiresOptIn
annotation class FaktorioExperimental

val Faktorio = createApplicationPlugin("Faktorio", ::FaktorioConfig) {
    application.attributes.put(SchemaRegistryKey, pluginConfig.registeredSchemas)
    application.attributes.put(ArgumentRegistryKey, pluginConfig.registeredParsers)
    onCall { call ->
        call.scopeHandler = pluginConfig.scopeHandler
    }
    on(MonitoringEvent(ApplicationStarted)) {
        SwaggerRoute(pluginConfig).route(it)
    }
}



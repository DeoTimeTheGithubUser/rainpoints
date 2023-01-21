package org.rain.rainpoints

import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import org.rain.rainpoints.argument.ArgumentRegistryKey
import org.rain.rainpoints.schemas.SchemaRegistryKey
import org.rain.rainpoints.scope.scopeHandler
import org.rain.rainpoints.swagger.SwaggerRoute

@DslMarker
annotation class RainpointsDsl

val Rainpoints = createApplicationPlugin("Rainpoints", ::RainpointsConfig) {
    application.attributes.put(SchemaRegistryKey, pluginConfig.registeredSchemas)
    application.attributes.put(ArgumentRegistryKey, pluginConfig.registeredParsers)
    onCall { call ->
        call.scopeHandler = pluginConfig.scopeHandler
    }
    on(MonitoringEvent(ApplicationStarted)) {
        SwaggerRoute(pluginConfig).route(it)
    }
}
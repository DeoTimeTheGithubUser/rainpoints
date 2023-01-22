package org.rain.rainpoints

import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.util.AttributeKey
import org.rain.rainpoints.argument.RainpointsArgumentRegistryKey
import org.rain.rainpoints.schemas.RainpointsSchemaRegistryKey
import org.rain.rainpoints.scope.RainpointsAPIScopes
import org.rain.rainpoints.scope.RainpointsScopeHandlers
import org.rain.rainpoints.swagger.SwaggerRoute

@DslMarker
annotation class RainpointsDsl

val Rainpoints = createApplicationPlugin("Rainpoints", ::RainpointsConfig) {
    fun <T : Any> register(key: AttributeKey<T>, value: T) = application.attributes.put(key, value)

    register(RainpointsSchemaRegistryKey, pluginConfig.registeredSchemas)
    register(RainpointsArgumentRegistryKey, pluginConfig.registeredParsers)
    register(RainpointsScopeHandlers, pluginConfig.scopeHandlers)
    register(RainpointsAPIScopes, pluginConfig.scopeLibs.flatMap { it.all() })
    on(MonitoringEvent(ApplicationStarted)) {
        SwaggerRoute(pluginConfig).route(it)
    }
}
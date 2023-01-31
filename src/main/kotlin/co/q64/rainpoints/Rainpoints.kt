package co.q64.rainpoints

import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.util.AttributeKey
import co.q64.rainpoints.argument.RainpointsArgumentRegistryKey
import co.q64.rainpoints.schemas.RainpointsSchemaRegistryKey
import co.q64.rainpoints.scope.RainpointsAPIScopeLibraries
import co.q64.rainpoints.scope.RainpointsScopeHandlers
import co.q64.rainpoints.swagger.SwaggerRoute

@DslMarker
annotation class RainpointsDsl

val Rainpoints = createApplicationPlugin("Rainpoints", ::RainpointsConfig) {
    fun <T : Any> register(key: AttributeKey<T>, value: T) = application.attributes.put(key, value)

    register(RainpointsSchemaRegistryKey, pluginConfig.registeredSchemas)
    register(RainpointsArgumentRegistryKey, pluginConfig.registeredParsers)
    register(RainpointsScopeHandlers, pluginConfig.scopeHandlers)
    register(RainpointsAPIScopeLibraries, pluginConfig.scopeLibs)
    on(MonitoringEvent(ApplicationStarted)) {
        SwaggerRoute(pluginConfig).route(it)
    }
}
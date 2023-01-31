package co.q64.rainpoints.scope

import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

internal val RainpointsAPIScopeLibraries = AttributeKey<List<APIScope.Library>>("RainpointsScopeRegistryKey")

val Application.scopes get() =
    attributes.getOrNull(RainpointsAPIScopeLibraries).orEmpty().flatMap { it.all() }
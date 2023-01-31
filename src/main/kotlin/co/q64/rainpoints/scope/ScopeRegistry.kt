package co.q64.rainpoints.scope

import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

internal val RainpointsAPIScopes = AttributeKey<List<APIScope>>("RainpointsScopeRegistryKey")

val Application.scopes get() = attributes.getOrNull(RainpointsAPIScopes).orEmpty()
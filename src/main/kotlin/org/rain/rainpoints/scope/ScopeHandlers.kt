package org.rain.rainpoints.scope

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import org.rain.rainpoints.ScopeHandler

internal val RainpointsScopeHandlers = AttributeKey<List<ScopeHandler>>("RainpointsScopeHandlersKey")

internal val Application.scopeHandlers: List<ScopeHandler>
    get() = attributes.getOrNull(RainpointsScopeHandlers).orEmpty()
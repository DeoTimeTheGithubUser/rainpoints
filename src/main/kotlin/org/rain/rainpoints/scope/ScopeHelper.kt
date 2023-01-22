package org.rain.rainpoints.scope

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import org.rain.rainpoints.ScopeHandler

private val RainpointsScopeHandler = AttributeKey<ScopeHandler>("RainpointsScopeHandlerKey")

internal var ApplicationCall.scopeHandler: ScopeHandler
    get() = attributes[RainpointsScopeHandler]
    internal set(value) = attributes.put(RainpointsScopeHandler, value)
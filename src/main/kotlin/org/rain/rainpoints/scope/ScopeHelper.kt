package org.rain.rainpoints.scope

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import org.rain.rainpoints.ScopeHandler

private val FaktorioScopeHandlerKey = AttributeKey<ScopeHandler>("FaktorioScopeHandlerKey")

internal var ApplicationCall.scopeHandler: ScopeHandler
    get() = attributes[FaktorioScopeHandlerKey]
    internal set(value) = attributes.put(FaktorioScopeHandlerKey, value)
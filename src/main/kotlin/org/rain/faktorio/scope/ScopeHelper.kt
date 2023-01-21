package org.rain.faktorio.scope

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import org.rain.faktorio.ScopeHandler

private val FaktorioScopeHandlerKey = AttributeKey<ScopeHandler>("FaktorioScopeHandlerKey")

internal var ApplicationCall.scopeHandler: ScopeHandler
    get() = attributes[FaktorioScopeHandlerKey]
    internal set(value) = attributes.put(FaktorioScopeHandlerKey, value)
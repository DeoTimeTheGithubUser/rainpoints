package co.q64.faktorio.internal

import co.q64.faktorio.ScopeHandler
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey

private val FaktorioScopeHandlerKey = AttributeKey<ScopeHandler>("FaktorioScopeHandlerKey")

internal var ApplicationCall.scopeHandler: ScopeHandler
    get() = attributes[FaktorioScopeHandlerKey]
    internal set(value) = attributes.put(FaktorioScopeHandlerKey, value)
package co.q64.faktorio

import co.q64.faktorio.internal.scopeHandler
import co.q64.faktorio.model.APIScope
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

val Faktorio = createApplicationPlugin("Faktorio", ::FaktorioConfig) {
    onCall { call ->
        call.scopeHandler = pluginConfig.scopeHandler
    }
}



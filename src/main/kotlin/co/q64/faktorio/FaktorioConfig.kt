package co.q64.faktorio

import co.q64.faktorio.argument.TypedArguments
import co.q64.faktorio.model.Endpoint
import kotlin.reflect.typeOf


class FaktorioConfig {
    internal var scopeHandler: ScopeHandler = { false }
    fun scoped(handler: ScopeHandler) {
        scopeHandler = handler
    }
    inline fun <reified T> argumentParser(default: Endpoint.Argument.Parser<T>) {
        TypedArguments[typeOf<T>()] = default
    }
}
package co.q64.faktorio


class FaktorioConfig {
    internal var scopeHandler: ScopeHandler = { false }
    fun scoped(handler: ScopeHandler) {
        scopeHandler = handler
    }
}
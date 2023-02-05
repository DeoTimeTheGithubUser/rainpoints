package co.q64.rainpoints.endpoint

import io.ktor.server.routing.Route
import io.ktor.util.AttributeKey

private val RouteCategoryKey = AttributeKey<String>("category")

var Route.category: String?
    get() = attributes.getOrNull(RouteCategoryKey)
    set(value) = if (value != null) attributes.put(RouteCategoryKey, value) else attributes.remove(RouteCategoryKey)
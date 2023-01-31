package co.q64.rainpoints.util

import io.ktor.server.routing.Route

val Route.path
    get() =
        generateSequence(this) { it.parent }
            .map { it.selector }
            .toList()
            .asReversed()
            .joinToString("/")

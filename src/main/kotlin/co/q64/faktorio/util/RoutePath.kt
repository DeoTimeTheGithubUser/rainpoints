package co.q64.faktorio.util

import io.ktor.server.routing.Route

val Route.path get() =
    generateSequence(this) { it.parent }
        .map { it.selector }
        .toList()
        .asReversed()
        .joinToString("/")

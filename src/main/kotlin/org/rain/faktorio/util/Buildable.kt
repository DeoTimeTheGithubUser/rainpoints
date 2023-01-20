package org.rain.faktorio.util

import io.ktor.server.application.Application

fun interface Buildable<T> {
    fun build(context: Application): T
}
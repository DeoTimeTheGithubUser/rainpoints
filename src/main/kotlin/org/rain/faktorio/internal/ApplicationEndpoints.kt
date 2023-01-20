package org.rain.faktorio.internal

import org.rain.faktorio.model.Endpoint
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

private val ApplicationEndpointsKey = AttributeKey<MutableList<Endpoint>>("ApplicationEndpoints")
internal val Application.endpoints get() = attributes.computeIfAbsent(ApplicationEndpointsKey, ::mutableListOf)
package org.rain.rainpoints.endpoint

import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

private val ApplicationEndpointsKey = AttributeKey<MutableList<Endpoint>>("ApplicationEndpoints")
internal val Application.endpoints get() = attributes.computeIfAbsent(ApplicationEndpointsKey, ::mutableListOf)
package co.q64.faktorio.internal

import co.q64.faktorio.model.Endpoint
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

private val ApplicationEndpointsKey = AttributeKey<MutableList<Endpoint>>("ApplicationEndpoints")
internal val Application.endpoints get() = attributes.computeIfAbsent(ApplicationEndpointsKey, ::mutableListOf)
package org.rain.faktorio.argument

import io.ktor.server.application.Application
import io.ktor.util.AttributeKey
import kotlinx.serialization.serializer
import org.rain.faktorio.endpoint.Endpoint
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal val ArgumentRegistryKey = AttributeKey<Map<KType, Endpoint.Argument.Parser<*>>>("ArgumentRegistry")

inline fun <reified T> Application.argumentParser() = argumentParser<T>(typeOf<T>())

@Suppress("UNCHECKED_CAST")
fun <T> Application.argumentParser(type: KType) =
    ((StandardArguments[type] ?: (attributes
        .getOrNull(ArgumentRegistryKey)
        ?.get(type)))
        ?: JsonArgumentParser(type, serializer(type))) as Endpoint.Argument.Parser<T>
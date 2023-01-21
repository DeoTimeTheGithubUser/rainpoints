package org.rain.rainpoints.schemas

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import kotlin.reflect.KClass

internal val SchemaRegistryKey = AttributeKey<Map<KClass<*>, SchemaConfiguration<*>>>("SchemaRegistry")
@Suppress("UNCHECKED_CAST")
fun <T : Any> Application.schemaConfiguration(type: KClass<T>) =
    attributes.getOrNull(SchemaRegistryKey)?.get(type) as? SchemaConfiguration<T>

fun <T : Any> Application.registeredSchema(type: KClass<T>) =
    schema(type).also { schemaConfiguration(type)?.apply { it.configure() } }

internal fun Application.schemaContent(type: KClass<*>) = registeredSchema(type).let {
    Content()
        .addMediaType(
            "${ContentType.Application.Json}",
            MediaType().schema(it)
        )
}
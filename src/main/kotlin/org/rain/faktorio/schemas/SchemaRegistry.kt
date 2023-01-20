package org.rain.faktorio.schemas

import io.ktor.server.application.Application
import io.ktor.util.AttributeKey
import kotlin.reflect.KClass

internal val SchemaRegistryKey = AttributeKey<Map<KClass<*>, SchemaConfiguration<*>>>("SchemaRegistry")
fun <T : Any> Application.schemaConfiguration(type: KClass<T>) =
    attributes.getOrNull(SchemaRegistryKey)?.get(type) as? SchemaConfiguration<T>

fun <T : Any> Application.registeredSchema(type: KClass<T>) =
    schema(type).also { schemaConfiguration(type)?.apply { it.configure() } }
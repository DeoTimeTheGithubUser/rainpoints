package org.rain.rainpoints.schemas

import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.XML
import java.io.File
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

internal object SchemaFactory {

    private val integerTypes = mapOf(
        Int::class to (Int.MIN_VALUE to Int.MAX_VALUE),
        Long::class to (Long.MIN_VALUE to Long.MAX_VALUE),
        Byte::class to (Byte.MIN_VALUE to Byte.MAX_VALUE),
        Short::class to (Short.MIN_VALUE to Short.MAX_VALUE)
    ) as Map<out KClass<out Any>, Pair<Number, Number>>

    private val floatingPointTypes = listOf(
        Float::class,
        Double::class
    )

    fun <T : Any> createSchema(clazz: KClass<T>) =
        Schema<T>().apply { useType(clazz.createType()) }

    fun <T, P> createProperty(prop: KProperty1<T, P>) =
        Schema<P>().apply { useType(prop.returnType) }

    private fun Schema<*>.useType(type: KType) {
        val clazz = (type.classifier as? KClass<*>) ?: error("Cannot make schema of generic type.")
        xml = XML().name(clazz.simpleName)
        when {
            // primitives
            clazz == String::class -> type("string")
            clazz == Boolean::class -> type("boolean")
            clazz in integerTypes -> {
                integerTypes[clazz]?.let { (min, max) ->
                    type("integer")
                    minimum = min.toLong().toBigDecimal()
                    maximum = max.toLong().toBigDecimal()
                }
            }
            clazz in floatingPointTypes -> type("number")
            clazz.java.isArray -> {
                type("array")
                clazz.java.componentType.kotlin.createType().let {
                    items(Schema<Any>().apply { useType(it) })
                }
            }

            // advanced types
            clazz == UUID::class -> type("string").format("uuid")
            clazz == File::class -> type("string").format("binary")
            clazz instance Iterable::class -> {
                type("array")
                type.arguments.firstOrNull()?.type?.let {
                    items(Schema<Any>().apply { useType(it) })
                }
            }
            clazz instance Enum::class -> {
                val values = clazz.java.enumConstants.toList()
                type("string")
                enum = values
            }
            clazz instance Map::class -> {
                type("object")
            }
            else -> {
                type("object")
                clazz.declaredMemberProperties.forEach {
                    addProperty(it.name, createProperty(it))
                }
            }
        }
    }

    private infix fun KClass<*>.instance(other: KClass<*>) =
        other.java.isAssignableFrom(java)
}
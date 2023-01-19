package co.q64.faktorio.schemas

import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.XML
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

@PublishedApi
internal class SchemaFactory {

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
        Schema<T>().apply {
            applyType(clazz.createType())
        }

    fun <T, P> createProperty(prop: KProperty1<T , P>) =
        Schema<P>().apply {
            applyType(prop.returnType)
        }

    private fun Schema<*>.applyType(type: KType) {
        val clazz = (type.classifier as? KClass<*>) ?: error("Cannot make schema of generic type.")
        xml = XML().name(clazz.simpleName)
        when {
            clazz instance Iterable::class -> {
                type("array")
                val elementType =
                    (type.arguments.firstOrNull()?.type?.classifier as? KClass<*>)
                xml.name(elementType?.simpleName)
            }
            clazz instance Enum::class -> {
                val values = clazz.java.enumConstants.toList()
                type("string")
                enum = values
            }
            clazz in integerTypes -> {
                integerTypes[clazz]?.let { (min, max) ->
                    type("integer")
                    minimum = min.toLong().toBigDecimal()
                    maximum = max.toLong().toBigDecimal()
                }
            }
            clazz in floatingPointTypes -> type("number")
            else -> type("object")
        }
        // this will definitely break so fix it at some point
        clazz.declaredMemberProperties.forEach {
            addProperty(it.name, createProperty(it))
        }
    }

    private infix fun KClass<*>.instance(other: KClass<*>) =
        other.java.isAssignableFrom(java)
}
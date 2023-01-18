package co.q64.faktorio.schema

import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

fun schema(type: KType) =
    FaktorioSchemaGenerator.generateSchema(type.javaType)
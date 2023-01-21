package org.rain.faktorio.util

import io.ktor.util.reflect.typeInfoImpl
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

internal val KType.clazz get() = classifier as KClass<*>
internal val KType.typeInfo get() =
    clazz.let { typeInfoImpl(it.java, it, this) }
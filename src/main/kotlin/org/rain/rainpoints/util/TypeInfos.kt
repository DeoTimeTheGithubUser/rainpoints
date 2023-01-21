package org.rain.rainpoints.util

import io.ktor.util.reflect.typeInfoImpl
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal val KType.clazz get() = classifier as KClass<*>
internal val KType.typeInfo get() =
    clazz.let { typeInfoImpl(it.java, it, this) }
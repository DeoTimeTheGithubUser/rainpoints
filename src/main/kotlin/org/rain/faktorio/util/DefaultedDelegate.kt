package org.rain.faktorio.util

import kotlin.properties.Delegates

fun <T> Delegates.defauled(default: T) = observable(default) { _, _, _ -> }
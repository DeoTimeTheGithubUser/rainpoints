package co.q64.faktorio.util

import io.ktor.http.HttpMethod
import io.swagger.v3.oas.models.PathItem
import kotlin.properties.ReadOnlyProperty

val HttpMethod.swag get() = PathItem.HttpMethod.valueOf(value.uppercase())
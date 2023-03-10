package co.q64.rainpoints.util

import io.ktor.http.HttpMethod
import io.swagger.v3.oas.models.PathItem

val HttpMethod.swag get() = PathItem.HttpMethod.valueOf(value.uppercase())
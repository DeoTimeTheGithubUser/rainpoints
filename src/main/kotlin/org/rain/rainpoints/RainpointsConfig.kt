package org.rain.rainpoints

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.info.Info
import org.rain.rainpoints.endpoint.Endpoint
import org.rain.rainpoints.schemas.SchemaConfiguration
import org.rain.rainpoints.scope.APIScope
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

class RainpointsConfig {

    internal var scopeHandler: ScopeHandler = { false }
    internal val registeredSchemas: MutableMap<KClass<*>, SchemaConfiguration<*>> = mutableMapOf()
    internal val registeredParsers: MutableMap<KType, Endpoint.Argument.Parser<*>> = mutableMapOf()
    internal var swagger: Swagger = Swagger()


    fun scoped(handler: ScopeHandler) {
        scopeHandler = handler
    }

    fun swagger(closure: Swagger.() -> Unit) {
        swagger = Swagger().apply(closure)
    }

    fun schemas(closure: SchemaRegistry.() -> Unit) {
        registeredSchemas.putAll(SchemaRegistry().apply(closure).schemas)
    }

    fun arguments(closure: ArgumentRegistry.() -> Unit) {
        registeredParsers.putAll(ArgumentRegistry().apply(closure).parsers)
    }

    data class Swagger(
        var docs: String = "/docs",
        var api: String = "api.json",
        var version: String = "4.14.0",
        var customStyle: String? = null,
        // Will display the description of an endpoint with the scope's description
        // if it is not already set
        var defaultDescriptionByScope: Boolean = false,
        var packageLocation: String = "https://unpkg.com/swagger-ui-dist",
        internal var info: Info = Info()
    ) {
        fun info(closure: Info.() -> Unit) {
            info = Info().apply(closure)
        }
    }

    @JvmInline
    value class ArgumentRegistry(
        val parsers: MutableMap<KType, Endpoint.Argument.Parser<*>> = mutableMapOf()
    ) {
        inline operator fun <reified T> Endpoint.Argument.Parser<T>.unaryPlus() {
            parsers[typeOf<T>()] = this
        }
    }

    @JvmInline
    value class SchemaRegistry(
        val schemas: MutableMap<KClass<*>, SchemaConfiguration<*>> = mutableMapOf()
    ) {
        inline operator fun <reified T> SchemaConfiguration<T>.unaryPlus() {
            schemas[T::class] = this
        }
    }
}
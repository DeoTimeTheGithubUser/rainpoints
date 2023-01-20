package org.rain.faktorio

import io.ktor.server.application.ApplicationCall
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.schemas.SchemaConfiguration
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.info.Info
import org.rain.faktorio.scope.APIScope
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

class FaktorioConfig {

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
        inline operator fun <reified T> SchemaConfiguration<T>.unaryPlus() = register(this)

        inline fun <reified T> register(config: SchemaConfiguration<T>) {
            schemas[T::class] = config
        }
    }
}
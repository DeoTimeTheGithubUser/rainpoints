package org.rain.faktorio

import io.ktor.server.application.ApplicationCall
import org.rain.faktorio.argument.TypedArguments
import org.rain.faktorio.model.Endpoint
import org.rain.faktorio.schemas.SchemaConfiguration
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.info.Info
import org.rain.faktorio.model.APIScope
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

class FaktorioConfig {

    internal var scopeHandler: ScopeHandler = { false }
    internal val registeredSchemas: MutableMap<KClass<*>, SchemaConfiguration<*>> = mutableMapOf()
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

    inline fun <reified T> argumentParser(default: Endpoint.Argument.Parser<T>) {
        TypedArguments[typeOf<T>()] = default
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
    value class SchemaRegistry(
        val schemas: MutableMap<KClass<*>, SchemaConfiguration<*>> = mutableMapOf()
    ) {
        inline operator fun <reified T> SchemaConfiguration<T>.unaryPlus() = register(this)

        inline fun <reified T> register(config: SchemaConfiguration<T>) {
            schemas[T::class] = config
        }
    }
}
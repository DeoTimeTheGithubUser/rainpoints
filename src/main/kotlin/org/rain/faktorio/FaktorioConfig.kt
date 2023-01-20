package org.rain.faktorio

import io.ktor.server.application.ApplicationCall
import org.rain.faktorio.argument.TypedArguments
import org.rain.faktorio.model.Endpoint
import org.rain.faktorio.schemas.SchemaConfiguration
import org.rain.faktorio.util.defauled
import io.ktor.server.plugins.swagger.SwaggerConfig
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.media.Schema
import org.rain.faktorio.model.APIScope
import kotlin.properties.Delegates
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

class FaktorioConfig {

    internal var scopeHandler: ScopeHandler = { false }
    internal var swagger = SwaggerConfig()
    internal val registeredSchemas: MutableMap<KClass<*>, SchemaConfiguration<*>> = mutableMapOf()

    var SwaggerConfig.docs by Delegates.defauled("/docs")
    var SwaggerConfig.api by Delegates.defauled("api.json")

    fun scoped(handler: ScopeHandler) {
        scopeHandler = handler
    }

    fun swagger(closure: SwaggerConfig.() -> Unit) {
        swagger = SwaggerConfig().apply(closure)
    }

    fun schemas(closure: SchemaRegistry.() -> Unit) {
        registeredSchemas.putAll(SchemaRegistry().apply(closure).schemas)
    }

    inline fun <reified T> argumentParser(default: Endpoint.Argument.Parser<T>) {
        TypedArguments[typeOf<T>()] = default
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
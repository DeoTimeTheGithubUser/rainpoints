package org.rain.rainpoints

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityScheme
import org.rain.rainpoints.endpoint.Endpoint
import org.rain.rainpoints.schemas.SchemaConfiguration
import org.rain.rainpoints.scope.APIScope
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias ScopeHandler = suspend PipelineContext<*, ApplicationCall>.(APIScope) -> Boolean

class RainpointsConfig {

    internal val registeredSchemas: MutableMap<KClass<*>, SchemaConfiguration<*>> = mutableMapOf()
    internal val registeredParsers: MutableMap<KType, Endpoint.Argument.Parser<*>> = mutableMapOf()
    internal val scopeLibs: MutableList<APIScope.Library> = mutableListOf()
    internal var scopeHandlers: MutableList<ScopeHandler> = mutableListOf()
    internal var swagger: Swagger = Swagger()


    fun scopes(closure: Scopes.() -> Unit) {
        val scopes = Scopes().apply(closure)
        scopeLibs += scopes.lib.scopes
        scopes.claim?.let { scopeHandlers += it }
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
        internal var info: Info = Info(),
        internal var oauth: OAuthFlow? = null
    ) {
        fun info(closure: Info.() -> Unit) {
            info = Info().apply(closure)
        }

        fun oauth(closure: OAuthFlow.() -> Unit) {
            oauth = OAuthFlow().apply(closure)
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

    data class Scopes(
        var lib: Libraries = Libraries(),
        var claim: ScopeHandler? = null
    ) {

        fun libraries(closure: Libraries.() -> Unit) {
            lib = Libraries().apply(closure)
        }

        fun claim(closure: ScopeHandler) {
            claim = closure
        }

        @JvmInline
        value class Libraries(
            val scopes: MutableSet<APIScope.Library> = mutableSetOf()
        ) {
            operator fun APIScope.Library.unaryPlus() {
                scopes += this
            }
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
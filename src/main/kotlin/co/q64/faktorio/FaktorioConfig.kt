package co.q64.faktorio

import co.q64.faktorio.argument.TypedArguments
import co.q64.faktorio.model.Endpoint
import co.q64.faktorio.schemas.SchemaConfiguration
import co.q64.faktorio.util.defauled
import io.ktor.server.plugins.swagger.SwaggerConfig
import kotlin.properties.Delegates
import kotlin.reflect.KClass
import kotlin.reflect.typeOf


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

    fun <T : Any> registerSchema(type: KClass<T>, config: SchemaConfiguration<T>) {
        registeredSchemas[type] = config
    }

    inline fun <reified T : Any> registerSchema(config: SchemaConfiguration<T>) =
        registerSchema(T::class, config)


    inline fun <reified T> argumentParser(default: Endpoint.Argument.Parser<T>) {
        TypedArguments[typeOf<T>()] = default
    }
}
package co.q64.rainpoints.schemas

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun interface SchemaConfiguration<out T> {
    fun Schema<@UnsafeVariance T>.configure()

    operator fun plus(other: SchemaConfiguration<@UnsafeVariance T>) = SchemaConfiguration {
        configure()
        with(other) { configure() }
    }

    companion object Default : SchemaConfiguration<Nothing> {
        override fun Schema<Nothing>.configure() = Unit
    }
}

fun <T : Any> schema(clazz: KClass<T>) = SchemaFactory.createSchema(clazz)
inline fun <reified T : Any> schema(config: SchemaConfiguration<T> = SchemaConfiguration) =
    with(config) { schema(T::class).apply { configure() } }

fun <T, P> Schema<T>.property(prop: KProperty1<T, P>, config: SchemaConfiguration<P> = SchemaConfiguration) {
    with(config) { addProperty(prop.name, SchemaFactory.createProperty(prop).also { it.configure() }) }
}

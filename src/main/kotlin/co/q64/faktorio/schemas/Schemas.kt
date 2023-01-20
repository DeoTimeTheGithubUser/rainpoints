package co.q64.faktorio.schemas

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun interface SchemaConfiguration<T> {
    fun Schema<T>.configure()
}

fun <T : Any> schema(clazz: KClass<T>) = SchemaFactory.createSchema(clazz)
inline fun <reified T : Any> schema(config: SchemaConfiguration<T>) =
    with(config) { schema(T::class).apply { configure() } }

fun <T, P> Schema<T>.property(prop: KProperty1<T, P>, config: SchemaConfiguration<P>) {
    with(config) { addProperty(prop.name, SchemaFactory.createProperty(prop).also { it.configure() }) }
}
package co.q64.faktorio.schemas

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KProperty1

@PublishedApi
internal val factory = SchemaFactory()

inline fun <reified T : Any> schema(closure: Schema<T>.() -> Unit) =
    factory.createSchema(T::class).apply(closure)

fun <T, P> Schema<T>.property(prop: KProperty1<T, P>, closure: Schema<P>.() -> Unit) =
    addProperty(prop.name, factory.createProperty(prop).apply(closure))
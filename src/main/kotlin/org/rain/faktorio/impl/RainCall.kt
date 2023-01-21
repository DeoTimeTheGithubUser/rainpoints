package org.rain.faktorio.impl

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import org.rain.faktorio.argument.StringArgumentParser
import org.rain.faktorio.endpoint.Endpoint
import org.rain.faktorio.endpoint.ExecutionHandler
import org.rain.faktorio.util.clazz
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

class RainCall(
    override val application: Application,
    @PublishedApi internal val responses: MutableList<RainResponse<*>> = mutableListOf(),
    @PublishedApi internal val arguments: MutableList<RainArgument<*>> = mutableListOf(),
    @PublishedApi internal var body: RainRequest<*>? = null,
    internal var bodyType: KType? = null,
    internal var resType: KType? = null,
    internal var execute: ExecutionHandler<*, *>? = null
) : Endpoint.Call._Internal {
    override fun parameter(
        name: String?,
        description: String?,
        type: Endpoint.Argument.Type
    ): Endpoint.Argument<String> =
        (RainArgument(application, name, type, StringArgumentParser, description))

    override fun <T : Any> response(
        code: HttpStatusCode,
        type: KClass<T>?,
        closure: Endpoint.Call.Response<T>.() -> Unit
    ) {
        responses += RainResponse(code, type = type).apply(closure)
    }

    override fun <T : Any> request(type: KClass<T>, closure: Endpoint.Call.Request<T>.() -> Unit) {
        body = RainRequest(type = type).apply(closure)
    }

    override fun <T> Endpoint.Argument<T>.provideDelegate(ref: Nothing?, prop: KProperty<*>): Endpoint.Argument<T> =
        (this as RainArgument<T>).run {
            (name?.let { this } ?: copy(name = prop.name))
        }.also { arguments += it }


    override operator fun <T> Endpoint.Argument<T>.getValue(ref: Nothing?, prop: KProperty<*>) =
        (this as? RainArgument<T>)?.value ?: error("Tried to access parameter $name's value outside of a request.")


    @OptIn(ExperimentalReflectionOnLambdas::class)
    override fun <B, R> execute(overload: Nothing?, handler: ExecutionHandler<B, R>) {
        val func = handler.reflect() ?: error("")
        func.valueParameters.firstOrNull()?.type?.takeUnless { it == NothingType }?.let {
            bodyType = it
            if (body == null) request(it.clazz)
        }
        func.returnType.takeUnless { it == UnitType }?.let {
            resType = it
            val clazz = it.clazz
            if (responses.none { it.type == clazz }) response(type = clazz)
        }
        execute = handler
    }

    internal fun configure() = apply {
        arguments.forEach { with(it.parser) { properties() } }
    }

    private companion object {
        val UnitType = typeOf<Unit>()
        val NothingType = ::error.returnType
    }

}
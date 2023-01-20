package org.rain.faktorio.impl

import org.rain.faktorio.FaktorioDsl
import org.rain.faktorio.argument.StringArgumentParser
import org.rain.faktorio.endpoint.Endpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import org.rain.faktorio.endpoint.RequestHandler
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class RainCall(
    override val application: Application,
    @PublishedApi internal val responses: MutableList<RainResponse<*>> = mutableListOf(),
    @PublishedApi internal val arguments: MutableList<RainArgument<*>> = mutableListOf(),
    @PublishedApi internal var body: RainBody<*>? = null,
    internal var request: RequestHandler? = null
) : Endpoint.Call {
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

    @JvmName("responseWithType")
    inline fun <reified T : Any> response(
        code: HttpStatusCode = HttpStatusCode.OK,
        closure: RainResponse<T>.() -> Unit
    ) {
        responses += RainResponse(code, type = T::class).apply(closure)
    }

    override fun <T : Any> body(type: KClass<T>, closure: Endpoint.Call.Body<T>.() -> Unit) {
        body = RainBody(type = type).apply(closure)
    }

    override fun <T> Endpoint.Argument<T>.provideDelegate(ref: Nothing?, prop: KProperty<*>): Endpoint.Argument<T> =
        (this as RainArgument<T>).run {
            (name?.let { this } ?: copy(name = prop.name))
        }.also { arguments += it }


    override operator fun <T> Endpoint.Argument<T>.getValue(ref: Nothing?, prop: KProperty<*>) =
        (this as? RainArgument<T>)?.value ?: error("Tried to access parameter $name's value outside of a request.")

    override fun request(closure: RequestHandler) {
        request = closure
    }

    internal fun configure() = apply {
        arguments.forEach { with(it.parser) { properties() } }
    }

}
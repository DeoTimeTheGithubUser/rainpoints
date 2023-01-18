package co.q64.faktorio.argument

import co.q64.faktorio.model.Endpoint
import io.ktor.http.HttpStatusCode
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private fun badRequest(reason: String): Endpoint.Call.() -> Unit = {
    response(HttpStatusCode.BadRequest) {
        description = reason
    }
}

@PublishedApi
internal val TypedArguments: MutableMap<KType, Endpoint.Argument.Parser<*>> =
    // Standard arguments
    listOf(
        StringArgumentParser,
        IntArgumentParser,
        LongArgumentParser,
        DoubleArgumentParser,
        BooleanArgumentParser,
        UUIDArgumentParser
    ).associateBy { it.type }.toMutableMap()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified T> typedArgument() =
    (TypedArguments[typeOf<T>()] as? Endpoint.Argument.Parser<T>)
        ?: throw IllegalArgumentException("No standard argument parser found for type ${typeOf<T>()}")

object StringArgumentParser : Endpoint.Argument.Parser<String> by (Endpoint.Argument.Parser { it })
object IntArgumentParser : Endpoint.Argument.Parser<Int> by (Endpoint.Argument.Parser { it.toInt() })
object LongArgumentParser : Endpoint.Argument.Parser<Long> by (Endpoint.Argument.Parser { it.toLong() })
object DoubleArgumentParser : Endpoint.Argument.Parser<Double> by (Endpoint.Argument.Parser { it.toDouble() })
object BooleanArgumentParser : Endpoint.Argument.Parser<Boolean> by (Endpoint.Argument.Parser { it.toBooleanStrict() })
object UUIDArgumentParser : Endpoint.Argument.Parser<UUID> by (Endpoint.Argument.Parser { UUID.fromString(it) })

fun <T : Comparable<T>> Endpoint.Argument<T>.min(min: T) = chain(badRequest("If the input value is less than $min")) {
    check(it > min) { "Input must be greater than $min" }
}
fun <T : Comparable<T>> Endpoint.Argument<T>.max(max: T) = chain(badRequest("If the input value is greter than $max")) {
    check(it < max) { "Input must be less than $max" }
}
fun Endpoint.Argument<Double>.finite() = chain(badRequest("If the input value is not finite")) {
    check(it.isFinite()) { "Input must be a finite value" }
}
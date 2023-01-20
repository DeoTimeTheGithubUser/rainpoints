package org.rain.faktorio.argument

import org.rain.faktorio.model.Endpoint
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
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
        ?: JsonArgumentParser(typeOf<T>(), serializer())

object StringArgumentParser : Endpoint.Argument.Parser<String> by (Endpoint.Argument.Parser { it })
object IntArgumentParser : Endpoint.Argument.Parser<Int> by (Endpoint.Argument.Parser(parse = String::toInt))
object LongArgumentParser : Endpoint.Argument.Parser<Long> by (Endpoint.Argument.Parser(parse = String::toLong))
object DoubleArgumentParser : Endpoint.Argument.Parser<Double> by (Endpoint.Argument.Parser(parse = String::toDouble))
object BooleanArgumentParser :
    Endpoint.Argument.Parser<Boolean> by (Endpoint.Argument.Parser(parse = String::toBooleanStrict))

object UUIDArgumentParser : Endpoint.Argument.Parser<UUID> by Endpoint.Argument.Parser(parse = UUID::fromString)

data class JsonArgumentParser<T>(
    override val type: KType,
    private val serializer: KSerializer<T>
) : Endpoint.Argument.Parser<T> {
    override fun parse(input: String) =
        Json.decodeFromString(serializer, input)
}

fun <T : Comparable<T>> Endpoint.Argument<T>.min(min: T) =
    require("Input value is less than $min") { it > min }

fun <T : Comparable<T>> Endpoint.Argument<T>.max(max: T) =
    require("Input is more than $max") { it < max }

fun Endpoint.Argument<Double>.finite() =
    require("Input value is not finite") { it.isFinite() }
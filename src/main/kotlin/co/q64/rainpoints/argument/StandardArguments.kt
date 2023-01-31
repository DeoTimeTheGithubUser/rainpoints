package co.q64.rainpoints.argument

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import co.q64.rainpoints.endpoint.Endpoint
import java.util.UUID
import kotlin.reflect.KType

@PublishedApi
internal val StandardArguments: Map<KType, Endpoint.Argument.Parser<*>> =
    listOf(
        StringArgumentParser,
        IntArgumentParser,
        LongArgumentParser,
        DoubleArgumentParser,
        BooleanArgumentParser,
        UUIDArgumentParser
    ).associateBy { it.type }

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
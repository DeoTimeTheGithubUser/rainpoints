package co.q64.rainpoints.argument

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import co.q64.rainpoints.endpoint.Endpoint
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.util.pipeline.PipelineContext
import java.math.BigDecimal
import java.math.BigInteger
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
        BigIntegerParser,
        BigDecimalParser,
        UUIDArgumentParser
    ).associateBy { it.type }

object StringArgumentParser : Endpoint.Argument.Parser<String> by (Endpoint.Argument.Parser { it })
object IntArgumentParser : Endpoint.Argument.Parser<Int> by (Endpoint.Argument.Parser.simple(parse = String::toInt))
object LongArgumentParser : Endpoint.Argument.Parser<Long> by (Endpoint.Argument.Parser.simple(parse = String::toLong))
object DoubleArgumentParser : Endpoint.Argument.Parser<Double> by (Endpoint.Argument.Parser.simple(parse = String::toDouble))
object BooleanArgumentParser : Endpoint.Argument.Parser<Boolean> by (Endpoint.Argument.Parser.simple(parse = String::toBooleanStrict))

object BigIntegerParser : Endpoint.Argument.Parser<BigInteger> by (Endpoint.Argument.Parser.simple(parse = reasonable(::BigInteger)))
object BigDecimalParser : Endpoint.Argument.Parser<BigDecimal> by (Endpoint.Argument.Parser.simple(parse = reasonable(::BigDecimal)))

object UUIDArgumentParser : Endpoint.Argument.Parser<UUID> by Endpoint.Argument.Parser.simple(parse = UUID::fromString)

data class JsonArgumentParser<T>(
    override val type: KType,
    private val serializer: KSerializer<T>
) : Endpoint.Argument.Parser<T> {
    override suspend fun PipelineContext<Unit, ApplicationCall>.parse(input: String): T =
        Json.decodeFromString(serializer, input)
}

fun <T : Comparable<T>> Endpoint.Argument<T>.min(min: T) =
    require("Input value is less than $min") { it > min }

fun <T : Comparable<T>> Endpoint.Argument<T>.max(max: T) =
    require("Input is more than $max") { it < max }

fun Endpoint.Argument<Double>.finite() =
    require("Input value is not finite") { it.isFinite() }

private const val ReasonableLength = 50
private fun <T : Number> reasonable(parse: (String) -> T): (String) -> T = {
    if (it.length > ReasonableLength) throw BadRequestException("Big numbers are limited to $ReasonableLength characters.")
    parse(it)
}

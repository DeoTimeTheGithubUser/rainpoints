package co.q64.faktorio.argument

import co.q64.faktorio.model.Endpoint
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal val TypedArguments: MutableMap<KType, Endpoint.Argument.Parser<*>> =
    // Standard arguments
    listOf(
        StringArgumentParser,
        IntArgumentParser(),
        LongArgumentParser(),
        DoubleArgumentParser(),
        BooleanArgumentParser,
        UUIDArgumentParser
    ).associateBy { it.type }.toMutableMap()

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified T> typedArgument() =
    (TypedArguments[typeOf<T>()] as? Endpoint.Argument.Parser<T>)
        ?: throw IllegalArgumentException("No standard argument parser found for type ${typeOf<T>()}")

object StringArgumentParser : Endpoint.Argument.Parser<String> by (Endpoint.Argument.Parser { it })

abstract class ComparableArgumentParser<T : Comparable<T>>(
    override val type: KType,
    val convert: (String) -> T,
) : Endpoint.Argument.Parser<T> {

    abstract val min: T
    abstract val max: T


    override fun parse(input: String): T {
        val item = convert(input)
        require(item > min) { "$type must be greater than minimum $min" }
        require(item < max) { "$type must be less than maximum $max" }
        return item
    }
}

data class IntArgumentParser(
    override val min: Int = Int.MIN_VALUE,
    override val max: Int = Int.MAX_VALUE
) : ComparableArgumentParser<Int>(typeOf<Int>(), String::toInt)

data class LongArgumentParser(
    override val min: Long = Long.MIN_VALUE,
    override val max: Long = Long.MAX_VALUE
) : ComparableArgumentParser<Long>(typeOf<Long>(), String::toLong)

data class DoubleArgumentParser(
    override val min: Double = Double.MIN_VALUE,
    override val max: Double = Double.MAX_VALUE,
    val finite: Boolean = true
) : ComparableArgumentParser<Double>(typeOf<Double>(), String::toDouble) {
    override fun parse(input: String) =
        super.parse(input).takeUnless { !it.isFinite() && finite }
            ?: throw IllegalArgumentException("Input must be a finite number.")
}

object BooleanArgumentParser : Endpoint.Argument.Parser<Boolean> by (Endpoint.Argument.Parser { it.toBooleanStrict() })
object UUIDArgumentParser : Endpoint.Argument.Parser<UUID> by (Endpoint.Argument.Parser(parse = UUID::fromString))
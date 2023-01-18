package co.q64.faktorio.argument

import co.q64.faktorio.model.Endpoint
import kotlin.reflect.typeOf

@PublishedApi
internal val StandardTypes =
    listOf(
        StringArgumentParser,
        IntArgumentParser(),
        BooleanArgumentParser
    ).associateBy { it.type }

@Suppress("UNCHECKED_CAST") @PublishedApi
internal inline fun <reified T> standardType() =
    (StandardTypes[typeOf<T>()] as? Endpoint.Argument.Parser<T>) ?: throw IllegalArgumentException("No standard argument parser found for type ${typeOf<T>()}")

object StringArgumentParser : Endpoint.Argument.Parser<String> by (Endpoint.Argument.Parser { it })
data class IntArgumentParser(
    val range: IntRange = (Int.MIN_VALUE..Int.MAX_VALUE)
) : Endpoint.Argument.Parser<Int> by Endpoint.Argument.Parser() {
    override fun parse(input: String): Int {
        val num = input.toInt()
        require(num > range.first) { "Integer must be greater than minimum ${range.first}" }
        require(num < range.last) { "Integer must be less than maximum ${range.last}" }
        return num
    }
}
object BooleanArgumentParser : Endpoint.Argument.Parser<Boolean> by (Endpoint.Argument.Parser { it.toBooleanStrict() })
package org.rain.faktorio.model

interface APIScope {

    val id: String
    val name: String
    val description: String
    val risk: Risk
    val parent: APIScope?

    operator fun String.invoke(builder: Builder.() -> Unit = {}): APIScope = Builder(this).apply(builder).let {
        Simple(
            this,
            this@APIScope,
            it.name,
            it.description,
            it.risk
        )
    }

    operator fun plus(other: APIScope): APIScope = TODO("???")

    data class Simple(
        override val id: String,
        override val parent: APIScope?,
        override val name: String,
        override val description: String,
        override val risk: Risk
    ) : APIScope

    data class Builder(
        var name: String,
        var description: String = "No description provided",
        var risk: Risk = Risk.Low
    )

    interface Library {
        operator fun String.invoke(builder: Builder.() -> Unit = {}): APIScope = with(Root) { invoke(builder) }

        companion object {
            val Root: APIScope = Simple(
                "api_root",
                null,
                "API Root",
                "Root API permission scope.",
                Risk.Extreme
            )
        }
    }

    enum class Risk {
        Low,
        Medium,
        High,
        Extreme
    }


}
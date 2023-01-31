package co.q64.rainpoints.scope

interface APIScope {

    val id: String
    val name: String
    val description: String
    val risk: Risk
    val parent: APIScope?
    val children: List<APIScope>
    val path: String

    operator fun String.invoke(builder: Builder.() -> Unit = {}): APIScope

    operator fun plus(other: APIScope): APIScope = TODO("???")

    data class Simple(
        override val id: String,
        override val parent: APIScope?,
        override val name: String,
        override val description: String,
        override val risk: Risk,
        override val children: MutableList<APIScope> = mutableListOf()
    ) : APIScope {
        override fun String.invoke(builder: Builder.() -> Unit) = Builder(this).apply(builder).let {
            Simple(
                this,
                this@Simple,
                it.name,
                it.description,
                it.risk
            )
        }.also { children += it }

        override val path: String
            get() = "${parent?.takeUnless { it.parent == null }?.let { "${it.id}:" } ?: ""}$id"
    }

    data class Builder(
        var name: String,
        var description: String = "No description provided",
        var risk: Risk = Risk.Low
    )

    interface Library {
        operator fun String.invoke(builder: Builder.() -> Unit = {}): APIScope = with(Root) { this@invoke.invoke(builder) }
        fun all(): List<APIScope> = Root.all()

        companion object {
            val Root: Library = object : Library {
                private val all = mutableListOf<APIScope>()
                private val rootScope = Simple(
                    "api_root",
                    null,
                    "API Root",
                    "Root API permission scope.",
                    Risk.Extreme
                )

                override fun String.invoke(builder: Builder.() -> Unit) =
                    with(rootScope) { invoke(builder) }.also { all += it }
                override fun all() = (all + all.flatMap { it.children })
            }
        }
    }

    enum class Risk {
        Low,
        Medium,
        High,
        Extreme
    }


}
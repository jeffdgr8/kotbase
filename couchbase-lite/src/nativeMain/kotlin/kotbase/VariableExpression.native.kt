package kotbase

public actual class VariableExpression
internal constructor(internal val name: String) : Expression() {

    override fun asJSON(): Any =
        listOf("?$name")
}

package com.couchbase.lite.kmp

public actual class VariableExpression
internal constructor(internal val name: String) : Expression() {

    override fun asJSON(): Any {
        return MutableArray().apply {
            addString("?$name")
        }
    }
}

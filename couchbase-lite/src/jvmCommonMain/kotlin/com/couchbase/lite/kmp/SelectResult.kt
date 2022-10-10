package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual open class SelectResult
private constructor(actual: com.couchbase.lite.SelectResult) :
    DelegatedClass<com.couchbase.lite.SelectResult>(actual) {

    public actual class From
    internal constructor(override val actual: com.couchbase.lite.SelectResult.From) :
        SelectResult(actual) {

        public actual fun from(alias: String): SelectResult = chain {
            actual.from(alias)
        }
    }

    public actual class As
    internal constructor(override val actual: com.couchbase.lite.SelectResult.As) :
        SelectResult(actual) {

        public actual fun `as`(alias: String): As = chain {
            actual.`as`(alias)
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(com.couchbase.lite.SelectResult.property(property))

        public actual fun expression(expression: Expression): As =
            As(com.couchbase.lite.SelectResult.expression(expression.actual))

        public actual fun all(): From =
            From(com.couchbase.lite.SelectResult.all())
    }
}

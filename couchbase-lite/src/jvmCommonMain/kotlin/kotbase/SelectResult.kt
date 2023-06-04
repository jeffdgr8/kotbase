package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.SelectResult as CBLSelectResult

public actual open class SelectResult
private constructor(actual: CBLSelectResult) : DelegatedClass<CBLSelectResult>(actual) {

    public actual class From
    internal constructor(override val actual: CBLSelectResult.From) :
        SelectResult(actual) {

        public actual fun from(alias: String): SelectResult {
            actual.from(alias)
            return this
        }
    }

    public actual class As
    internal constructor(override val actual: CBLSelectResult.As) :
        SelectResult(actual) {

        public actual fun `as`(alias: String): As {
            actual.`as`(alias)
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(CBLSelectResult.property(property))

        public actual fun expression(expression: Expression): As =
            As(CBLSelectResult.expression(expression.actual))

        public actual fun all(): From =
            From(CBLSelectResult.all())
    }
}

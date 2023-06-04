package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Ordering as CBLOrdering

public actual abstract class Ordering
private constructor(actual: CBLOrdering) : DelegatedClass<CBLOrdering>(actual) {

    public actual class SortOrder
    internal constructor(override val actual: CBLOrdering.SortOrder) :
        Ordering(actual) {

        public actual fun ascending(): Ordering {
            actual.ascending()
            return this
        }

        public actual fun descending(): Ordering {
            actual.descending()
            return this
        }
    }

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(CBLOrdering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(CBLOrdering.expression(expression.actual))
    }
}

package kotbase

import cocoapods.CouchbaseLite.CBLQueryOrdering
import cocoapods.CouchbaseLite.CBLQuerySortOrder
import kotbase.base.DelegatedClass

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual abstract class Ordering
private constructor(actual: CBLQueryOrdering) : DelegatedClass<CBLQueryOrdering>(actual) {

    public actual class SortOrder
    internal constructor(override val actual: CBLQuerySortOrder) : Ordering(actual) {

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
            SortOrder(CBLQueryOrdering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(CBLQueryOrdering.expression(expression.actual))
    }
}

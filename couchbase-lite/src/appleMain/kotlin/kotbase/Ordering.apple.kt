package kotbase

import cocoapods.CouchbaseLite.CBLQueryOrdering
import cocoapods.CouchbaseLite.CBLQuerySortOrder
import kotlin.Array

internal actual class OrderingPlatformState(
    internal val actual: CBLQueryOrdering
)

public actual sealed class Ordering
private constructor(actual: CBLQueryOrdering) {

    internal actual val platformState = OrderingPlatformState(actual)

    public actual class SortOrder
    internal constructor(internal val actual: CBLQuerySortOrder) : Ordering(actual) {

        public actual fun ascending(): Ordering {
            actual.ascending()
            return this
        }

        public actual fun descending(): Ordering {
            actual.descending()
            return this
        }
    }

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Ordering)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(CBLQueryOrdering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(CBLQueryOrdering.expression(expression.actual))
    }
}

internal val Ordering.actual: CBLQueryOrdering
    get() = platformState.actual

// TODO: casting the existing property fails with:
// Undefined symbols for architecture arm64:
//   "_OBJC_CLASS_$_CBLQuerySortOrder", referenced from:
//       objc-class-ref in test.kexe.o
// ld: symbol(s) not found for architecture arm64
//internal val Ordering.SortOrder.actual: CBLQuerySortOrder
//    get() = platformState.actual as CBLQuerySortOrder

internal fun Array<out Ordering>.actuals(): List<CBLQueryOrdering> =
    map { it.actual }

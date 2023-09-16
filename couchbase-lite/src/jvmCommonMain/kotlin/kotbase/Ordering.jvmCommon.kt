/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotlin.Array
import com.couchbase.lite.Ordering as CBLOrdering

internal actual class OrderingPlatformState(
    internal val actual: CBLOrdering
)

public actual sealed class Ordering
private constructor(actual: CBLOrdering) {

    internal actual val platformState = OrderingPlatformState(actual)

    public actual class SortOrder
    internal constructor(actual: CBLOrdering.SortOrder) : Ordering(actual) {

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
        actual == (other as? Ordering)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()

    public actual companion object {

        public actual fun property(property: String): SortOrder =
            SortOrder(CBLOrdering.property(property))

        public actual fun expression(expression: Expression): SortOrder =
            SortOrder(CBLOrdering.expression(expression.actual))
    }
}

internal val Ordering.actual: CBLOrdering
    get() = platformState.actual

internal val Ordering.SortOrder.actual: CBLOrdering.SortOrder
    get() = platformState.actual as CBLOrdering.SortOrder

internal fun Array<out Ordering>.actuals(): Array<CBLOrdering> =
    map { it.actual }.toTypedArray()

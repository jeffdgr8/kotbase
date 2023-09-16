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
import com.couchbase.lite.SelectResult as CBLSelectResult

internal actual class SelectResultPlatformState(
    internal val actual: CBLSelectResult
)

public actual open class SelectResult
private constructor(actual: CBLSelectResult) {

    internal actual val platformState = SelectResultPlatformState(actual)

    public actual class From
    internal constructor(actual: CBLSelectResult.From) : SelectResult(actual) {

        public actual fun from(alias: String): SelectResult {
            actual.from(alias)
            return this
        }
    }

    public actual class As
    internal constructor(actual: CBLSelectResult.As) : SelectResult(actual) {

        public actual fun `as`(alias: String): As {
            actual.`as`(alias)
            return this
        }
    }

    override fun equals(other: Any?): Boolean =
        actual == (other as? SelectResult)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()

    public actual companion object {

        public actual fun property(property: String): As =
            As(CBLSelectResult.property(property))

        public actual fun expression(expression: Expression): As =
            As(CBLSelectResult.expression(expression.actual))

        public actual fun all(): From =
            From(CBLSelectResult.all())
    }
}

internal val SelectResult.actual: CBLSelectResult
    get() = platformState.actual

internal val SelectResult.From.actual: CBLSelectResult.From
    get() = platformState.actual as CBLSelectResult.From

internal val SelectResult.As.actual: CBLSelectResult.As
    get() = platformState.actual as CBLSelectResult.As

internal fun Array<out SelectResult>.actuals(): Array<CBLSelectResult> =
    map { it.actual }.toTypedArray()

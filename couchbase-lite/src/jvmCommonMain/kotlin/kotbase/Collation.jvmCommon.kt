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

import com.couchbase.lite.Collation as CBLCollation

internal actual class CollationPlatformState(
    internal val actual: CBLCollation
)

public actual sealed class Collation
private constructor(actual: CBLCollation) {

    internal actual val platformState = CollationPlatformState(actual)

    public actual class ASCII
    internal constructor(actual: CBLCollation.ASCII) : Collation(actual) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            actual.setIgnoreCase(ignCase)
            return this
        }
    }

    public actual class Unicode
    internal constructor(actual: CBLCollation.Unicode) : Collation(actual) {

        public actual fun setLocale(locale: String?): Unicode {
            actual.setLocale(locale)
            return this
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode {
            actual.setIgnoreAccents(ignAccents)
            return this
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode {
            actual.setIgnoreCase(ignCase)
            return this
        }
    }

    override fun equals(other: Any?): Boolean =
        actual == (other as? Collation)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()

    public actual companion object {

        public actual fun ascii(): ASCII =
            ASCII(CBLCollation.ascii())

        public actual fun unicode(): Unicode =
            Unicode(CBLCollation.unicode())
    }
}

internal val Collation.actual: CBLCollation
    get() = platformState.actual

internal val Collation.ASCII.actual: CBLCollation.ASCII
    get() = platformState.actual as CBLCollation.ASCII

internal val Collation.Unicode.actual: CBLCollation.Unicode
    get() = platformState.actual as CBLCollation.Unicode
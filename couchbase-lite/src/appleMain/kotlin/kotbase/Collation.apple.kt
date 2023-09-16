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

import cocoapods.CouchbaseLite.CBLQueryCollation

internal actual class CollationPlatformState(
    internal var actual: CBLQueryCollation
)

public actual sealed class Collation
private constructor(actual: CBLQueryCollation) {

    internal actual val platformState = CollationPlatformState(actual)

    public actual class ASCII
    internal constructor(
        actual: CBLQueryCollation = CBLQueryCollation.asciiWithIgnoreCase(false)
    ) : Collation(actual) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            actual = CBLQueryCollation.asciiWithIgnoreCase(ignCase)
            return this
        }
    }

    public actual class Unicode
    internal constructor(
        actual: CBLQueryCollation = CBLQueryCollation
            .unicodeWithLocale(null, ignoreCase = false, ignoreAccents = false)
    ) : Collation(actual) {

        private var locale: String? = null
        private var ignCase: Boolean = false
        private var ignAccents: Boolean = false

        public actual fun setLocale(locale: String?): Unicode {
            this.locale = locale
            actual = CBLQueryCollation.unicodeWithLocale(locale, ignCase, ignAccents)
            return this
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode {
            this.ignAccents = ignAccents
            actual = CBLQueryCollation.unicodeWithLocale(locale, ignCase, ignAccents)
            return this
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode {
            this.ignCase = ignCase
            actual = CBLQueryCollation.unicodeWithLocale(locale, ignCase, ignAccents)
            return this
        }
    }

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Collation)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()

    public actual companion object {

        public actual fun ascii(): ASCII = ASCII()

        public actual fun unicode(): Unicode = Unicode()
    }
}

internal var Collation.actual: CBLQueryCollation
    get() = platformState.actual
    set(value) {
        platformState.actual = value
    }

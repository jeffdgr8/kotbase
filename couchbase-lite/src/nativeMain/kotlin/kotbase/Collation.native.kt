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

internal actual class CollationPlatformState(
    private val isUnicode: Boolean,
    internal var locale: String? = null,
    internal var ignAccents: Boolean = false,
    internal var ignCase: Boolean = false
) {

    internal fun asJSON(): Map<String, Any?> {
        return mapOf(
            "UNICODE" to isUnicode,
            "LOCALE" to locale,
            "CASE" to !ignCase,
            "DIAC" to !ignAccents
        )
    }
}

public actual sealed class Collation
private constructor(isUnicode: Boolean) {

    internal actual val platformState = CollationPlatformState(isUnicode)

    public actual class ASCII : Collation(false) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            platformState.ignCase = ignCase
            return this
        }
    }

    public actual class Unicode : Collation(true) {

        public actual fun setLocale(locale: String?): Unicode {
            platformState.locale = locale
            return this
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode {
            platformState.ignAccents = ignAccents
            return this
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode {
            platformState.ignCase = ignCase
            return this
        }
    }

    public actual companion object {

        public actual fun ascii(): ASCII = ASCII()

        public actual fun unicode(): Unicode = Unicode()
    }
}

internal fun Collation.asJSON() = platformState.asJSON()

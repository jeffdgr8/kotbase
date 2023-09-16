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

import java.util.*
import com.couchbase.lite.FullTextIndex as CBLFullTextIndex

public actual class FullTextIndex
internal constructor(actual: CBLFullTextIndex) : Index(actual) {

    public actual fun setLanguage(language: String?): FullTextIndex {
        actual.setLanguage(language)
        return this
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var language: String? = Locale.getDefault().language
        set(value) {
            field = value
            actual.setLanguage(value)
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex {
        actual.ignoreAccents(ignoreAccents)
        return this
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var isIgnoringAccents: Boolean = false
        set(value) {
            field = value
            actual.ignoreAccents(value)
        }
}

internal val FullTextIndex.actual: CBLFullTextIndex
    get() = platformState!!.actual as CBLFullTextIndex

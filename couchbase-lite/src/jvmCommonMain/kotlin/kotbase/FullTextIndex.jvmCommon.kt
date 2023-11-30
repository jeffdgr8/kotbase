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

import com.couchbase.lite.FullTextIndex as CBLFullTextIndex

public actual class FullTextIndex
internal constructor(override val actual: CBLFullTextIndex) : Index(actual) {

    public actual fun setLanguage(language: String?): FullTextIndex {
        actual.setLanguage(language)
        return this
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual.setLanguage(value)
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex {
        actual.ignoreAccents(ignoreAccents)
        return this
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.isIgnoringAccents
        set(value) {
            actual.ignoreAccents(value)
        }
}

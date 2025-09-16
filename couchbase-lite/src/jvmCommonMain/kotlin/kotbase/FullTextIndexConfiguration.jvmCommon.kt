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

import com.couchbase.lite.FullTextIndexConfiguration as CBLFullTextIndexConfiguration

public actual class FullTextIndexConfiguration
private constructor(override val actual: CBLFullTextIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(CBLFullTextIndexConfiguration(*expressions))

    public actual constructor(expressions: List<String>) : this(CBLFullTextIndexConfiguration(expressions))

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        actual.language = language
        return this
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual.language = value
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        actual.ignoreAccents(ignoreAccents)
        return this
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.isIgnoringAccents
        set(value) {
            actual.ignoreAccents(value)
        }
}

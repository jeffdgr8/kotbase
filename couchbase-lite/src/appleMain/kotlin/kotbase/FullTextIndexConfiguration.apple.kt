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

import cocoapods.CouchbaseLite.CBLFullTextIndexConfiguration
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.currentLocale

public actual class FullTextIndexConfiguration
private constructor(actual: CBLFullTextIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        CBLFullTextIndexConfiguration(
            expressions.toList(),
            false,
            NSLocale.currentLocale.objectForKey(NSLocaleLanguageCode) as String?
        )
    )

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        this.language = language
        return this
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual = CBLFullTextIndexConfiguration(
                actual.expressions,
                actual.ignoreAccents,
                value
            )
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.ignoreAccents
        set(value) {
            actual = CBLFullTextIndexConfiguration(
                actual.expressions,
                value,
                actual.language
            )
        }
}

internal var FullTextIndexConfiguration.actual: CBLFullTextIndexConfiguration
    get() = platformState!!.actual as CBLFullTextIndexConfiguration
    set(value) {
        platformState!!.actual = value
    }

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

/**
 * Index for Full-Text search
 */
public expect class FullTextIndex : Index {

    /**
     * The language code which is an ISO-639 language such as "en", "fr", etc.
     * Setting the language code affects how word breaks and word stems are parsed.
     * Without setting the value, the current locale's language will be used. Setting
     * a nil or "" value to disable the language features.
     */
    public fun setLanguage(language: String?): FullTextIndex

    public var language: String?

    /**
     * Set the true value to ignore accents/diacritical marks. The default value is false.
     */
    public fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex

    public var isIgnoringAccents: Boolean
}

/*
 * Copyright (c) 2020 MOLO17
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/main/java/com/molo17/couchbase/lite/DocumentExtensions.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Resolve explicitApiWarning() requirements
 */

package kotbase.ktx

import kotbase.*

/**
 * Creates a new [MutableDocument] with the key-value entries specified by the
 * given [block] function.
 *
 * Example of usage:
 *
 * ```
 * val document = MutableDocument {
 *   "name" to "John"
 *   "surname" to "Doe"
 *   "type" to "user"
 * }
 * ```
 *
 * @return a [MutableDocument] instance
 */
public fun MutableDocument(block: DocumentBuilder.() -> Unit): MutableDocument =
    DocumentBuilder().apply(block).build()

public fun MutableDocument(id: String?, block: DocumentBuilder.() -> Unit): MutableDocument =
    DocumentBuilder(id).apply(block).build()

public class DocumentBuilder
private constructor(private val document: MutableDocument) : MutableDictionaryInterface by document {

    internal constructor(id: String? = null) : this(MutableDocument(id))

    internal fun build() = document

    /**
     * Determines the key-to-value relation between the receiver string and the provided [value].
     */
    public infix fun String.to(value: Any?) {
        setValue(this, value)
    }
}

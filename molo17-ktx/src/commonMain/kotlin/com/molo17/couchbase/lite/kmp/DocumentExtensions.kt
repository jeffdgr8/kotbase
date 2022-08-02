/*
 * Copyright (c) 2020 MOLO17
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
 * Modified by Jeff Lockhart
 *
 * - Use com.couchbase.lite.kmp package for couchbase-lite-kmp Kotlin Multiplatform bindings
 */

package com.molo17.couchbase.lite.kmp

import com.couchbase.lite.kmp.MutableDocument

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
@Suppress("FunctionName")
fun MutableDocument(block: DocumentBuilder.() -> Unit): MutableDocument =
    DocumentBuilder().apply(block).build()

class DocumentBuilder internal constructor(
    private val document: MutableDocument = MutableDocument()
) {

    internal fun build() = document

    /**
     * Determines the key-to-value relation between the receiver string and the provided [value].
     */
    infix fun <T> String.to(value: T) {
        document.setValue(this, value)
    }
}
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
package com.couchbase.lite

import cocoapods.CouchbaseLite.c4Doc
import cocoapods.CouchbaseLite.kDocExists
import kotbase.Dictionary
import kotbase.Document
import kotbase.MutableDictionary
import kotbase.actual
import kotlinx.cinterop.pointed

@Suppress("UNCHECKED_CAST")
internal actual val Document.content: Dictionary
    get() = MutableDictionary(actual.toDictionary() as Map<String, Any?>)

internal actual fun Document.exists(): Boolean {
    val c4Doc = actual.c4Doc?.rawDoc ?: return false
    return (c4Doc.pointed.flags and kDocExists) != 0u
}

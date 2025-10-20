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

import kotbase.*
import kotbase.internal.fleece.toKString
import libcblite.CBLDocument_GetRevisionHistory
import libcblite.CBLDocument_Retain

internal actual val Document.content: Dictionary
    get() = Dictionary(properties, dbContext, release = false)

internal actual fun Document.exists(): Boolean {
    // TODO: use CBLDocument_Exists private API once added to SDK
    return true
}

internal actual val Document.revisionHistory: String?
    get() = debug.CBLDocument_GetRevisionHistory(actual).toKString()

internal actual fun Document.copyImmutable(): Document {
    return toMutable().also {
        // conflict resolver will release, so retain to keep alive
        CBLDocument_Retain(it.actual)
    }
}

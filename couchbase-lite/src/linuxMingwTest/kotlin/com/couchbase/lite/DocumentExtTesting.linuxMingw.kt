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
import kotlinx.cinterop.*
import libcblite.C4Document
import libcblite.kDocExists
import kotlin.experimental.ExperimentalNativeApi

internal actual val Document.content: Dictionary
    get() = Dictionary(properties, dbContext)

internal actual fun Document.exists(): Boolean {
    val c4Doc = c4Doc ?: return false
    return (c4Doc.pointed.flags and kDocExists) != 0u
}

@OptIn(ExperimentalNativeApi::class)
internal val Document.c4Doc: CPointer<C4Document>?
    get() {
        // TODO: have Couchbase add private API CBLDocument_Exists, similar to CBLDocument_Generation
        // hack to address _c4doc field of C++ object CBLDocument
        // C4Document* is 2nd field (CBLDatabase* is 1st, both pointers)
        // found to be at index 12 for Windows and 8 for Linux (1st is index 3 for both)
        val offset = when (Platform.osFamily) {
            OsFamily.LINUX -> 8
            OsFamily.WINDOWS -> 12
            else -> error("Unhandled OS: ${Platform.osFamily}")
        }
        val ptrs = actual.reinterpret<LongVar>()
        return ptrs[offset].toCPointer()
    }

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

import kotbase.Document
import kotbase.MutableDocument
import libcblite.CBLDocument_Generation

internal actual val Document.generation: Long
    get() {
        val generation = CBLDocument_Generation(actual).toLong()
        return if (this is MutableDocument) {
            // assume MutableDocument is mutated, which expects
            // incremented generation (good enough for tests)
            generation + 1
        } else {
            generation
        }
    }

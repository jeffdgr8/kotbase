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

import kotbase.Blob
import kotbase.Database
import kotbase.asBlob
import kotbase.ext.wrapCBLError

internal actual fun Database.saveBlob(blob: Blob) {
    wrapCBLError { error ->
        actual.saveBlob(blob.actual, error)
    }
}

@Suppress("UNCHECKED_CAST")
internal actual fun Database.getBlob(props: Map<String, Any?>): Blob? {
    if (!Blob.isBlob(props)) {
        throw IllegalArgumentException("getBlob arg does not specify a blob")
    }
    return actual.getBlob(props as Map<Any?, *>)?.asBlob()
}

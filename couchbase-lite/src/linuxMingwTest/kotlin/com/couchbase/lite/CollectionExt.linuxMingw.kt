/*
 * Copyright 2023 Jeff Lockhart
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

import kotbase.Collection
import kotbase.Document
import kotbase.internal.fleece.toList
import kotbase.internal.wrapCBLError
import libcblite.CBLCollection_GetIndexesInfo
import libcblite.FLMutableArray_Release

// TODO: implement native C getC4Document()

internal actual fun Collection.getC4Document(id: String): C4Document? =
    C4Document(getDocument(id))

internal actual class C4Document(private val doc: Document?) {

    actual val isRevDeleted: Boolean
        get() = doc == null
}

internal actual fun Collection.getIndexInfo(): List<Map<String, Any?>> {
    val flIndexInfo = database.withLock {
        wrapCBLError { error ->
            CBLCollection_GetIndexesInfo(actual, error)
        }
    }
    @Suppress("UNCHECKED_CAST")
    return (flIndexInfo?.toList(null) as List<Map<String, Any?>>).also {
        FLMutableArray_Release(flIndexInfo)
    }
}

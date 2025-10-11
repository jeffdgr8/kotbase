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
import kotbase.internal.DbContext
import kotbase.internal.wrapCBLError
import libcblite.CBLDatabase_GetBlob
import libcblite.CBLDatabase_SaveBlob

internal actual fun Database.saveBlob(blob: Blob) {
    wrapCBLError { error ->
        CBLDatabase_SaveBlob(actual, blob.actual, error)
    }
    blob.checkSetDb(DbContext(this))
}

internal actual fun Database.getBlob(props: Map<String, Any?>): Blob? {
    require(Blob.isBlob(props)) { "getBlob arg does not specify a blob" }
    return try {
        wrapCBLError { error ->
            val dict = MutableDictionary(props)
            CBLDatabase_GetBlob(actual, dict.actual, error)
                ?.asBlob(DbContext(this))
        }
    } catch (e: CouchbaseLiteException) {
        println(e)
        null
    }
}

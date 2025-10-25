/*
 * Copyright 2024 Jeff Lockhart
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

import kotbase.internal.DbContext
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.convert
import libcblite.CBLQueryIndex_BeginUpdate

@Throws(CouchbaseLiteException::class)
public actual fun QueryIndex.beginUpdate(limit: Int): IndexUpdater? {
    require(limit > 0) { "limit must be > 0" }
    return wrapCBLError { error ->
        CBLQueryIndex_BeginUpdate(actual, limit.convert(), error)
            ?.asIndexUpdater(DbContext(collection.database))
    }
}

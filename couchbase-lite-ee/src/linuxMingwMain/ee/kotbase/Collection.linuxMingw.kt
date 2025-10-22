/*
 * Copyright 2025 Jeff Lockhart
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

import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.memScoped
import libcblite.CBLCollection_CreateArrayIndex
import libcblite.CBLCollection_CreateFullTextIndex
import libcblite.CBLCollection_CreateValueIndex
import libcblite.CBLCollection_CreateVectorIndex

internal fun Collection.createIndexImpl(name: String, config: IndexConfiguration) {
    try {
        wrapCBLError { error ->
            memScoped {
                when (config) {
                    is ValueIndexConfiguration -> CBLCollection_CreateValueIndex(
                        actual,
                        name.toFLString(this),
                        config.actual(this),
                        error
                    )
                    is FullTextIndexConfiguration -> CBLCollection_CreateFullTextIndex(
                        actual,
                        name.toFLString(this),
                        config.actual(this),
                        error
                    )
                    is ArrayIndexConfiguration -> CBLCollection_CreateArrayIndex(
                        actual,
                        name.toFLString(this),
                        config.actual(this),
                        error
                    )
                    is VectorIndexConfiguration -> CBLCollection_CreateVectorIndex(
                        actual,
                        name.toFLString(this),
                        config.actual(this),
                        error
                    )
                }
            }
        }
    } catch (e: CouchbaseLiteException) {
        if (e.domain == CBLError.Domain.CBLITE && e.code == CBLError.Code.INVALID_PARAMETER) {
            throw IllegalArgumentException(e.message, e)
        } else {
            throw e
        }
    }
}

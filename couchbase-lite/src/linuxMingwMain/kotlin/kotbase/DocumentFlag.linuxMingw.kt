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
package kotbase

import libcblite.CBLDocumentFlags
import libcblite.kCBLDocumentFlagsAccessRemoved
import libcblite.kCBLDocumentFlagsDeleted

public actual enum class DocumentFlag {
    DELETED,
    ACCESS_REMOVED
}

internal fun CBLDocumentFlags.toDocumentFlags(): Set<DocumentFlag> = buildSet {
    val flags = this@toDocumentFlags
    if (flags and kCBLDocumentFlagsDeleted != 0U) {
        add(DocumentFlag.DELETED)
    }
    if (flags and kCBLDocumentFlagsAccessRemoved != 0U) {
        add(DocumentFlag.ACCESS_REMOVED)
    }
}

internal fun Set<DocumentFlag>.toCBLDocumentFlags(): CBLDocumentFlags {
    var flags = 0U
    if (contains(DocumentFlag.DELETED)) {
        flags = flags or kCBLDocumentFlagsDeleted
    }
    if (contains(DocumentFlag.ACCESS_REMOVED)) {
        flags = flags or kCBLDocumentFlagsAccessRemoved
    }
    return flags
}

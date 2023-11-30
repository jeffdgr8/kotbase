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
package kotbase

import kotbase.internal.fleece.toKString
import kotbase.util.toList
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import libcblite.CBLCollectionChange

public actual class CollectionChange
internal constructor(
    internal val actual: CPointer<CBLCollectionChange>,
    public actual val collection: Collection
) {

    public actual val documentIDs: List<String> =
        with(actual.pointed) {
            docIDs!!.toList(numDocs) { it.pointed.toKString()!! }
        }
}

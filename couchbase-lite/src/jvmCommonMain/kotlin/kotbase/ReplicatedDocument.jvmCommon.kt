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

import kotbase.internal.DelegatedClass
import com.couchbase.lite.ReplicatedDocument as CBLReplicatedDocument

public actual class ReplicatedDocument
internal constructor(actual: CBLReplicatedDocument) : DelegatedClass<CBLReplicatedDocument>(actual) {

    public actual val scope: String
        get() = actual.collectionScope

    public actual val collection: String
        get() = actual.collectionName

    public actual val id: String
        get() = actual.id

    public actual val flags: Set<DocumentFlag>
        get() = actual.flags

    public actual val error: CouchbaseLiteException?
        get() = actual.error
}

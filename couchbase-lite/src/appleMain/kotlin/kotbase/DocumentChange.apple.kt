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

import cocoapods.CouchbaseLite.CBLDocumentChange
import kotbase.base.DelegatedClass

public actual class DocumentChange
internal constructor(actual: CBLDocumentChange) : DelegatedClass<CBLDocumentChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database!!)
    }

    public actual val documentID: String
        get() = actual.documentID!!
}

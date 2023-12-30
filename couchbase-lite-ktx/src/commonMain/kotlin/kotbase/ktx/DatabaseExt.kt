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
package kotbase.ktx

import kotbase.Database
import kotbase.Document
import kotbase.documentChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * A flow of a document's current state.
 * Emits null if document is deleted.
 * [filterNotNull] if this is undesired.
 *
 * @param id Document ID
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use getDefaultCollection().documentFlow()",
    ReplaceWith("getDefaultCollection()!!.documentFlow(id)")
)
public fun Database.documentFlow(id: String): Flow<Document?> = documentFlow(id, Dispatchers.IO)

/**
 * A flow of a document's current state.
 * Emits null if document is deleted.
 * [filterNotNull] if this is undesired.
 *
 * @param id Document ID
 * @param fetchContext CoroutineContext to fetch the document on; defaults to Dispatchers.IO
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use getDefaultCollection().documentFlow()",
    ReplaceWith("getDefaultCollection()!!.documentFlow(id, fetchContext)")
)
public fun Database.documentFlow(
    id: String,
    fetchContext: CoroutineContext
): Flow<Document?> = flow {
    val doc = withContext(fetchContext) {
        getDocument(id)
    }
    emit(doc)
    val changes = documentChangeFlow(id).map {
        it.database.getDocument(it.documentID)
    }.flowOn(fetchContext)
    emitAll(changes)
}

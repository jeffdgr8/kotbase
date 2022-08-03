package com.udobny.kmp.couchbase.lite.ktx

import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.Document
import com.couchbase.lite.kmp.documentChangeFlow
import kotlinx.coroutines.flow.*

/**
 * A flow of a document's current state.
 * Emits null if document is deleted.
 * [filterNotNull] if this is undesired.
 */
public fun Database.documentFlow(id: String): Flow<Document?> {
    return flow {
        val doc = getDocument(id)
        emit(doc)
        val changes = documentChangeFlow(id).map {
            it.database.getDocument(it.documentID)
        }
        emitAll(changes)
    }
}

package kotbase.ktx

import kotbase.Database
import kotbase.Document
import kotbase.documentChangeFlow
import kotlinx.coroutines.flow.*

/**
 * A flow of a document's current state.
 * Emits null if document is deleted.
 * [filterNotNull] if this is undesired.
 */
public fun Database.documentFlow(id: String): Flow<Document?> = flow {
    val doc = getDocument(id)
    emit(doc)
    val changes = documentChangeFlow(id).map {
        it.database.getDocument(it.documentID)
    }
    emitAll(changes)
}
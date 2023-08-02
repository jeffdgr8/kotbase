package kotbase

import com.couchbase.lite.ConflictHandler as CBLConflictHandler

internal fun ConflictHandler.convert(): CBLConflictHandler =
    CBLConflictHandler { document, oldDocument ->
        invoke(MutableDocument(document), oldDocument?.asDocument())
    }

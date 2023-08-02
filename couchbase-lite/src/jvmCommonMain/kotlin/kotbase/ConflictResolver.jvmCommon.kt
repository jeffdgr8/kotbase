package kotbase

import com.couchbase.lite.ConflictResolver as CBLConflictResolver

internal fun ConflictResolver.convert(): CBLConflictResolver =
    CBLConflictResolver { conflict ->
        invoke(Conflict(conflict))?.actual
    }

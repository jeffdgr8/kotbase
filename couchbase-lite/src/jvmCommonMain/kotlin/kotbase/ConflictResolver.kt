@file:JvmName("ConflictResolverJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.ConflictResolver as CBLConflictResolver

internal fun ConflictResolver.convert(): CBLConflictResolver =
    CBLConflictResolver { conflict ->
        invoke(Conflict(conflict))?.actual
    }

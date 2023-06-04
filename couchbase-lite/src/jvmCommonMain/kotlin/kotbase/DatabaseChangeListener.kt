@file:JvmName("DatabaseChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.DatabaseChangeListener as CBLDatabaseChangeListener

internal fun DatabaseChangeListener.convert(): CBLDatabaseChangeListener =
    CBLDatabaseChangeListener { change ->
        invoke(DatabaseChange(change))
    }

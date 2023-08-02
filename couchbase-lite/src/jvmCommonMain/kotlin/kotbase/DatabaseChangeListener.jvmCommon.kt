package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.DatabaseChangeListener as CBLDatabaseChangeListener

internal fun DatabaseChangeListener.convert(): CBLDatabaseChangeListener =
    CBLDatabaseChangeListener { change ->
        invoke(DatabaseChange(change))
    }

internal fun DatabaseChangeSuspendListener.convert(scope: CoroutineScope): CBLDatabaseChangeListener =
    CBLDatabaseChangeListener { change ->
        scope.launch {
            invoke(DatabaseChange(change))
        }
    }

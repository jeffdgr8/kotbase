package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.QueryChangeListener as CBLQueryChangeListener

internal fun QueryChangeListener.convert(): CBLQueryChangeListener =
    CBLQueryChangeListener { change ->
        invoke(QueryChange(change))
    }

internal fun QueryChangeSuspendListener.convert(scope: CoroutineScope): CBLQueryChangeListener =
    CBLQueryChangeListener { change ->
        scope.launch {
            invoke(QueryChange(change))
        }
    }

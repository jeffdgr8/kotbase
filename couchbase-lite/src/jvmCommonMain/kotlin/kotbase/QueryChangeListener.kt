@file:JvmName("QueryChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.QueryChangeListener as CBLQueryChangeListener

internal fun QueryChangeListener.convert(): CBLQueryChangeListener =
    CBLQueryChangeListener { change ->
        invoke(QueryChange(change))
    }

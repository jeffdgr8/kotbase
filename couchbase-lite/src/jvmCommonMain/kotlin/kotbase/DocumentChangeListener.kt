@file:JvmName("DocumentChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.DocumentChangeListener as CBLDocumentChangeListener

internal fun DocumentChangeListener.convert(): CBLDocumentChangeListener =
    CBLDocumentChangeListener { change ->
        invoke(DocumentChange(change))
    }

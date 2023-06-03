@file:JvmName("DocumentChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

internal fun DocumentChangeListener.convert(): com.couchbase.lite.DocumentChangeListener {
    return com.couchbase.lite.DocumentChangeListener { change ->
        invoke(DocumentChange(change))
    }
}

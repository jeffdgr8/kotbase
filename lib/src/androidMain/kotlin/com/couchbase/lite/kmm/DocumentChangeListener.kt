@file:JvmName("DocumentChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmm

internal fun DocumentChangeListener.convert(): com.couchbase.lite.DocumentChangeListener {
    return com.couchbase.lite.DocumentChangeListener { change ->
        invoke(DocumentChange(change))
    }
}

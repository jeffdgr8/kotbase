@file:JvmName("DatabaseChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmm

internal fun DatabaseChangeListener.convert(): com.couchbase.lite.DatabaseChangeListener {
    return com.couchbase.lite.DatabaseChangeListener { change ->
        invoke(DatabaseChange(change))
    }
}

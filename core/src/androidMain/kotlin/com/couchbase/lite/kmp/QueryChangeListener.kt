@file:JvmName("QueryChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun QueryChangeListener.convert(): com.couchbase.lite.QueryChangeListener {
    return com.couchbase.lite.QueryChangeListener { change ->
        invoke(QueryChange(change))
    }
}

package com.couchbase.lite.kmp

import cnames.structs.CBLListenerToken
import kotlinx.cinterop.CPointer

public actual interface ListenerToken

internal class DelegatedListenerToken(
    val actual: CPointer<CBLListenerToken>,
    val type: ListenerTokenType,
    val index: Int
) : ListenerToken

internal enum class ListenerTokenType {
    DATABASE,
    DOCUMENT,
    QUERY,
    REPLICATOR,
    DOCUMENT_REPLICATION
}

internal fun <T> addChangeListener(
    changeListeners: MutableList<ChangeListener<T>?>,
    listener: ChangeListener<T>
): Int {
    var index = changeListeners.indexOf(null)
    if (index < 0) {
        changeListeners.add(listener)
        index = changeListeners.lastIndex
    } else {
        changeListeners[index] = listener
    }
    return index
}

internal fun removeChangeListener(changeListeners: MutableList<in Nothing?>, index: Int) {
    changeListeners[index] = null
    while (changeListeners.isNotEmpty() && changeListeners.last() == null) {
        changeListeners.removeLast()
    }
}

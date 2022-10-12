package com.couchbase.lite.kmp

import cnames.structs.CBLListenerToken
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef

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

internal fun <T : Any> addListener(
    listeners: MutableList<StableRef<T>?>,
    listener: T
): Pair<Int, COpaquePointer> {
    val stableRef = StableRef.create(listener)
    var index = listeners.indexOf(null)
    if (index < 0) {
        listeners.add(stableRef)
        index = listeners.lastIndex
    } else {
        listeners[index] = stableRef
    }
    return Pair(index, stableRef.asCPointer())
}

internal fun <T : Any> removeListener(listeners: MutableList<StableRef<T>?>, index: Int): Boolean {
    if (index > listeners.lastIndex) return false
    val stableRef = listeners[index] ?: return false
    stableRef.dispose()
    listeners[index] = null
    while (listeners.isNotEmpty() && listeners.last() == null) {
        listeners.removeLast()
    }
    return true
}

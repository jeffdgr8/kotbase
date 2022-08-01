package com.couchbase.lite

import cocoapods.CouchbaseLite.c4Doc
import cocoapods.CouchbaseLite.generation
import cocoapods.CouchbaseLite.kDocExists
import com.couchbase.lite.kmm.Dictionary
import com.couchbase.lite.kmm.Document
import com.couchbase.lite.kmm.MutableDictionary
import kotlinx.cinterop.*

@Suppress("UNCHECKED_CAST")
internal actual val Document.content: Dictionary
    get() = MutableDictionary(actual.toDictionary() as Map<String, Any?>)

internal actual fun Document.exists(): Boolean {
    val c4Doc = actual.c4Doc?.rawDoc ?: return false
    return (c4Doc.pointed.flags and kDocExists) != 0u
}

internal actual fun Document.generation(): Long =
    actual.generation().convert()

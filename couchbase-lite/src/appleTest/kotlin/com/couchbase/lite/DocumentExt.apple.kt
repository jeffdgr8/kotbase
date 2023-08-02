package com.couchbase.lite

import cocoapods.CouchbaseLite.c4Doc
import cocoapods.CouchbaseLite.kDocExists
import kotbase.Dictionary
import kotbase.Document
import kotbase.MutableDictionary
import kotlinx.cinterop.pointed

@Suppress("UNCHECKED_CAST")
internal actual val Document.content: Dictionary
    get() = MutableDictionary(actual.toDictionary() as Map<String, Any?>)

internal actual fun Document.exists(): Boolean {
    val c4Doc = actual.c4Doc?.rawDoc ?: return false
    return (c4Doc.pointed.flags and kDocExists) != 0u
}

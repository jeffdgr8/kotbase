package com.couchbase.lite

import cocoapods.CouchbaseLite.CBLDocument
import com.couchbase.lite.kmm.Dictionary
import com.couchbase.lite.kmm.Document
import com.couchbase.lite.kmm.MutableDictionary
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCMethod
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import platform.darwin.NSObject
import platform.darwin.NSUInteger
import platform.posix.u_int32_tVar

@Suppress("UNCHECKED_CAST")
internal actual val Document.content: Dictionary
    get() = MutableDictionary(actual.toDictionary() as Map<String, Any?>)

internal actual fun Document.exists(): Boolean {
    val cblC4Doc = actual.c4Doc() ?: return false // CBLC4Document
    val c4doc = cblC4Doc.rawDoc()                 // C4Document
    val flags = c4doc[4]                          // C4DocumentFlags
    return (flags and 0x1000u) != 0u              // kDocExists
}

@ObjCMethod("c4Doc", "@16@0:8")
private external fun CBLDocument.c4Doc(): NSObject? // CBLC4Document?

@ObjCMethod("rawDoc", "@16@0:8")
private external fun NSObject.rawDoc(): CPointer<u_int32_tVar> // CBLC4Document.rawDoc(): C4Document

internal actual fun Document.generation(): Long =
    actual.generation().convert()

@ObjCMethod("generation", "@16@0:8")
private external fun CBLDocument.generation(): NSUInteger

package com.couchbase.lite

import com.couchbase.lite.kmp.Dictionary
import com.couchbase.lite.kmp.Document
import kotlinx.cinterop.*
import libcblite.C4Document
import libcblite.CBLDocument_Generation
import libcblite.kDocExists

internal actual val Document.content: Dictionary
    get() = Dictionary(properties)

internal actual fun Document.exists(): Boolean {
    // TODO: have Couchbase add private API CBLDocument_Exists, similar to CBLDocument_Generation
    // hack to address _c4doc field of C++ object CBLDocument
    val memMask = 0xFFFFFFFFFF000000UL.toLong()
    val memPrefix = actual.rawValue.toLong() and memMask
    val ptrs = actual.reinterpret<LongVar>()
    var i = 0
    var found = 0
    val c4Doc: CPointer<C4Document>
    while (true) {
        val ptr = ptrs[i++]
        if (ptr and memMask == memPrefix) {
            found++
        }
        // C4Document* is 2nd field (CBLDatabase* is 1st, both pointers)
        // found to be at index 12 for Windows and 8 for Linux (1st is index 3 for both)
        if (found == 2) {
            c4Doc = ptr.toCPointer() ?: return false
            break
        }
    }
    return (c4Doc.pointed.flags and kDocExists) != 0u
}

internal actual fun Document.generation(): Long =
    CBLDocument_Generation(actual).toLong()

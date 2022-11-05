package com.couchbase.lite

import com.couchbase.lite.kmp.Dictionary
import com.couchbase.lite.kmp.Document
import com.couchbase.lite.kmp.MutableDocument
import kotlinx.cinterop.*
import libcblite.C4Document
import libcblite.CBLDocument_Generation
import libcblite.kDocExists

internal actual val Document.content: Dictionary
    get() = Dictionary(properties, dbContext)

internal actual fun Document.exists(): Boolean {
    val c4Doc = c4Doc ?: return false
    return (c4Doc.pointed.flags and kDocExists) != 0u
}

internal val Document.c4Doc: CPointer<C4Document>?
    get() {
        // TODO: have Couchbase add private API CBLDocument_Exists, similar to CBLDocument_Generation
        // hack to address _c4doc field of C++ object CBLDocument
        // C4Document* is 2nd field (CBLDatabase* is 1st, both pointers)
        // found to be at index 12 for Windows and 8 for Linux (1st is index 3 for both)
        val offset = when (Platform.osFamily) {
            OsFamily.LINUX -> 8
            OsFamily.WINDOWS -> 12
            else -> error("Unhandled OS: ${Platform.osFamily}")
        }
        val ptrs = actual.reinterpret<LongVar>()
        return ptrs[offset].toCPointer()
    }

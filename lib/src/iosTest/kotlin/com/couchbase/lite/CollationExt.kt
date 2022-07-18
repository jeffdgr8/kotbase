package com.couchbase.lite

import cocoapods.CouchbaseLite.CBLQueryCollation
import com.couchbase.lite.kmm.Collation
import kotlinx.cinterop.ObjCMethod

actual fun Collation.asJSON(): Any? =
    actual.asJSON()

// TODO: replace with .def pending https://github.com/JetBrains/kotlin/pull/4894
@ObjCMethod("asJSON", "@16@0:8")
private external fun CBLQueryCollation.asJSON(): Any?

package com.couchbase.lite

import cocoapods.CouchbaseLite.CBLQueryExpression
import com.couchbase.lite.kmm.Expression
import kotlinx.cinterop.ObjCMethod

actual fun Expression.asJSON(): Any? =
    actual.asJSON()

// TODO: replace with .def pending https://github.com/JetBrains/kotlin/pull/4894
@ObjCMethod("asJSON", "@16@0:8")
private external fun CBLQueryExpression.asJSON(): Any?

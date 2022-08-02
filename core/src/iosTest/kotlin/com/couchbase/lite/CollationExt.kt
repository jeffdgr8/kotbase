package com.couchbase.lite

import cocoapods.CouchbaseLite.asJSON
import com.couchbase.lite.kmp.Collation

actual fun Collation.asJSON(): Any? =
    actual.asJSON()

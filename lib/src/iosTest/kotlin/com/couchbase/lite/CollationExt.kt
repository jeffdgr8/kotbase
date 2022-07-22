package com.couchbase.lite

import cocoapods.CouchbaseLite.asJSON
import com.couchbase.lite.kmm.Collation

actual fun Collation.asJSON(): Any? =
    actual.asJSON()

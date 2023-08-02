package com.couchbase.lite

import cocoapods.CouchbaseLite.asJSON
import kotbase.Collation

actual fun Collation.asJSON(): Any? =
    actual.asJSON()

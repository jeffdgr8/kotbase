package com.couchbase.lite

import cocoapods.CouchbaseLite.asJSON
import com.couchbase.lite.kmp.Expression

actual fun Expression.asJSON(): Any? =
    actual.asJSON()

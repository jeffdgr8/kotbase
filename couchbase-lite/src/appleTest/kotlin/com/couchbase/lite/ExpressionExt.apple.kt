package com.couchbase.lite

import cocoapods.CouchbaseLite.asJSON
import kotbase.Expression

actual fun Expression.asJSON(): Any? =
    actual.asJSON()

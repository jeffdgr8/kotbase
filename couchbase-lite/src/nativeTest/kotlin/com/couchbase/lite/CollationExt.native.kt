package com.couchbase.lite

import kotbase.Collation

actual fun Collation.asJSON(): Any? =
    platformState.asJSON()

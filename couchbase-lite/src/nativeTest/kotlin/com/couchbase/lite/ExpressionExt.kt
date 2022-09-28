package com.couchbase.lite

import com.couchbase.lite.kmp.Expression

actual fun Expression.asJSON(): Any? =
    asJSON()

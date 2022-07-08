package com.udobny.kmm.ext

import platform.Foundation.NSNumber

public fun NSNumber.asNumber(): Number {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return when (val any = this as Any) {
        // NSNumber can be kotlin.Boolean if created as a boolean
        is Boolean -> if (any) 1 else 0
        else -> this as Number
    }
}

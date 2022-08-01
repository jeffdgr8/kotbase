package com.udobny.kmm

import kotlinx.datetime.Instant

/**
 * [Instant.toString] does not include ".000" millis
 * for times that do not include millisecond
 * precision (or happen to land on a whole second).
 */
public fun Instant.toStringWithMillis(): String {
    return toString().let {
        if (it.length == 20) {
            it.dropLast(1) + ".000Z"
        } else {
            it
        }
    }
}

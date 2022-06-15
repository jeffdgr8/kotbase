package com.udobny.kmm.ext

import platform.Foundation.*

public fun NSNumber.toNumber(): Number {
    val int = intValue
    val long = longLongValue
    val float = floatValue
    val double = doubleValue
    return when {
        double.toLong().toDouble() != double -> when {
            float.toDouble() != double -> double
            else -> float
        }
        int.toLong() != long -> long
        else -> int
    }
}

public fun Number.toNSNumber(): NSNumber {
    // Number may be an NSNumber https://kotlinlang.org/docs/native-objc-interop.html#nsnumber
    @Suppress("USELESS_IS_CHECK")
    if (this is NSNumber) return this
    return when (this) {
        is Byte -> NSNumber.numberWithChar(this)
        is Short -> NSNumber.numberWithShort(this)
        is Int -> NSNumber.numberWithInt(this)
        is Long -> NSNumber.numberWithLongLong(this)
        is Float -> NSNumber.numberWithFloat(this)
        is Double -> NSNumber.numberWithDouble(this)
        else -> error("Unknown type")
    }
}

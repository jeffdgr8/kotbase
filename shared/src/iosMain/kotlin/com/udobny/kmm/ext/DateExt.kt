package com.udobny.kmm.ext

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDate

public actual fun Any.toNativeDateDeep(): Any {
    return when (this) {
        is Instant -> toNSDate()
        is List<*> -> toNativeDatesDeep()
        is Map<*, *> -> toNativeDateValuesDeep()
        else -> this
    }
}

public actual fun List<*>.toNativeDatesDeep(): List<*> =
    map { it?.toNativeDateDeep() }

public actual fun <K> Map<K, *>.toNativeDateValuesDeep(): Map<K, *> =
    mapValues { it.value?.toNativeDateDeep() }

public fun Any.toKotlinInstantDeep(): Any {
    return when (this) {
        is NSDate -> toKotlinInstant()
        is List<*> -> toKotlinInstantsDeep()
        is Map<*, *> -> toKotlinInstantValuesDeep()
        else -> this
    }
}

public fun List<*>.toKotlinInstantsDeep(): List<*> =
    map { it?.toKotlinInstantDeep() }

public fun <K> Map<K, *>.toKotlinInstantValuesDeep(): Map<K, *> =
    mapValues { it.value?.toKotlinInstantDeep() }

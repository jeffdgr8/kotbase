package com.udobny.kmm.ext

import kotlinx.datetime.Instant
import java.util.*

public fun Instant.toDate(): Date = Date(toEpochMilliseconds())

public fun Date.toKotlinInstant(): Instant = Instant.fromEpochMilliseconds(time)

public actual fun Any.toNativeDateDeep(): Any {
    return when (this) {
        is Instant -> toDate()
        is List<*> -> toNativeDatesDeep()
        is Map<*, *> -> toNativeDateValuesDeep()
        else -> this
    }
}

public actual fun List<*>.toNativeDatesDeep(): List<*> =
    map { it?.toNativeDateDeep() }

public actual fun <K> Map<K, *>.toNativeDateValuesDeep(): Map<K, *> =
    mapValues { it.value?.toNativeDateDeep() }

public actual fun Any.toKotlinInstantDeep(): Any {
    return when (this) {
        is Date -> toKotlinInstant()
        is List<*> -> toKotlinInstantsDeep()
        is Map<*, *> -> toKotlinInstantValuesDeep()
        else -> this
    }
}

public actual fun List<*>.toKotlinInstantsDeep(): List<*> =
    map { it?.toKotlinInstantDeep() }

public actual fun <K> Map<K, *>.toKotlinInstantValuesDeep(): Map<K, *> =
    mapValues { it.value?.toKotlinInstantDeep() }

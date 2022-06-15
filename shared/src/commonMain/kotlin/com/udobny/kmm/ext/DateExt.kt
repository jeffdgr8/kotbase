package com.udobny.kmm.ext

public expect fun Any.toNativeDateDeep(): Any

public expect fun List<*>.toNativeDatesDeep(): List<*>

public expect fun <K> Map<K, *>.toNativeDateValuesDeep(): Map<K, *>

public expect fun Any.toKotlinInstantDeep(): Any

public expect fun List<*>.toKotlinInstantsDeep(): List<*>

public expect fun <K> Map<K, *>.toKotlinInstantValuesDeep(): Map<K, *>

package com.udobny.kmp

import kotlinx.cinterop.*

public inline operator fun <reified T : CStructVar> CPointer<T>?.plus(index: Int): CPointer<T>? =
    interpretCPointer(this.rawValue + index * sizeOf<T>())

public inline operator fun <reified T : CStructVar> CPointer<T>.get(index: Int): T =
    (this + index)!!.pointed

public inline fun <reified T : CStructVar, R> CPointer<T>.toList(
    size: Int,
    transform: (CPointer<T>) -> R
): List<R> {
    val array = this
    return buildList {
        repeat(size) { index ->
            add(transform(array[index].ptr))
        }
    }
}

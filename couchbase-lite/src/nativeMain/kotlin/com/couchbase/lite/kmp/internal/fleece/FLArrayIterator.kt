package com.couchbase.lite.kmp.internal.fleece

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import libcblite.*

internal fun FLArray.iterator(memScope: MemScope): Iterator<FLValue> =
    FLArrayKIterator(this, memScope)

private class FLArrayKIterator(array: FLArray, memScope: MemScope) : Iterator<FLValue> {

    private val itr = memScope.alloc<FLArrayIterator>()

    init {
        FLArrayIterator_Begin(array, itr.ptr)
    }

    override fun hasNext(): Boolean =
        FLArrayIterator_GetValue(itr.ptr) != null

    override fun next(): FLValue {
        val value = FLArrayIterator_GetValue(itr.ptr) ?: throw NoSuchElementException()
        FLArrayIterator_Next(itr.ptr)
        return value
    }
}

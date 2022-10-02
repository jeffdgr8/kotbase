package com.couchbase.lite.kmp.internal.fleece

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import libcblite.*

internal fun FLDict.iterator(memScope: MemScope): Iterator<Pair<String, FLValue>> =
    FLDictKIterator(this, memScope)

private class FLDictKIterator(
    dict: FLDict,
    memScope: MemScope
) : Iterator<Pair<String, FLValue>> {

    private val itr = memScope.alloc<FLDictIterator>()

    init {
        FLDictIterator_Begin(dict, itr.ptr)
    }

    override fun hasNext(): Boolean =
        FLDictIterator_GetValue(itr.ptr) != null

    override fun next(): Pair<String, FLValue> {
        val key = FLDictIterator_GetKeyString(itr.ptr).toKString() ?: throw NoSuchElementException()
        val value = FLDictIterator_GetValue(itr.ptr)!!
        FLDictIterator_Next(itr.ptr)
        return Pair(key, value)
    }
}

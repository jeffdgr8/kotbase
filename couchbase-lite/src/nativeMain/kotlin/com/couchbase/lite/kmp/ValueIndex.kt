package com.couchbase.lite.kmp

import kotlinx.cinterop.*
import libcblite.CBLValueIndexConfiguration
import libcblite.kCBLN1QLLanguage

public actual class ValueIndex
internal constructor(private val items: List<ValueIndexItem>) : Index() {

    private fun getJson(): String {
        return MutableArray().apply {
            items.forEach {
                addValue(it.expression.asJSON())
            }
        }.toJSON()
    }

    internal fun getActual(memScope: MemScope): CValue<CBLValueIndexConfiguration> {
        val expCstr = getJson().cstr
        return cValue {
            expressionLanguage = kCBLN1QLLanguage
            expressions.buf = expCstr.getPointer(memScope)
            expressions.size = expCstr.size.convert()
        }
    }
}

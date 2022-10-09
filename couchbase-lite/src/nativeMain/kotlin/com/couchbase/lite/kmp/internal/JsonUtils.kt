package com.couchbase.lite.kmp.internal

import com.couchbase.lite.kmp.MutableArray
import com.couchbase.lite.kmp.MutableDictionary
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.fleece.toObject
import com.couchbase.lite.kmp.internal.fleece.wrapFLError
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import libcblite.FLDoc_FromJSON
import libcblite.FLDoc_GetRoot
import libcblite.FLDoc_Release
import libcblite.FLValue_ToJSON

internal object JsonUtils {

    fun parseJson(json: String): Any? {
        val doc = wrapFLError { error ->
            memScoped {
                FLDoc_FromJSON(json.toFLString(this), error)
            }
        }
        return FLDoc_GetRoot(doc)?.toObject(null, false).also {
            FLDoc_Release(doc)
        }
    }

    fun toJson(map: Map<String, Any?>): String =
        FLValue_ToJSON(MutableDictionary(map).actual.reinterpret()).toKString()!!

    fun toJson(list: List<Any?>): String =
        FLValue_ToJSON(MutableArray(list).actual.reinterpret()).toKString()!!
}

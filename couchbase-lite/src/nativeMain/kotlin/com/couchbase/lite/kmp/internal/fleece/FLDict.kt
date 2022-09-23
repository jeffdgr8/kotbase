package com.couchbase.lite.kmp.internal.fleece

import com.couchbase.lite.kmp.Blob
import kotlinx.cinterop.memScoped
import kotlinx.datetime.Instant
import libcblite.*

internal fun FLDict.toMap(): Map<String, Any?> {
    return buildMap {
        memScoped {
            this@toMap.iterator(this).forEach {
                put(it.first, it.second.toObject())
            }
        }
    }
}

internal fun FLDict.keys(): List<String> {
    return memScoped {
        iterator(this)
            .asSequence()
            .map { it.first }
            .toList()
    }
}

internal fun FLDict.getValue(key: String, isMutable: Boolean): Any? =
    FLDict_Get(this, key.toFLString())
        ?.toNative(isMutable)

internal fun FLDict.getString(key: String): String? =
    getValue(key, false) as? String

internal fun FLDict.getNumber(key: String): Number? {
    return when (val value = getValue(key, false)) {
        is Number -> value
        is Boolean -> if (value) 1 else 0
        else -> null
    }
}

internal fun FLDict.getInt(key: String): Int =
    getNumber(key)?.toInt() ?: 0

internal fun FLDict.getLong(key: String): Long =
    getNumber(key)?.toLong() ?: 0L

internal fun FLDict.getFloat(key: String): Float =
    getNumber(key)?.toFloat() ?: 0F

internal fun FLDict.getDouble(key: String): Double =
    getNumber(key)?.toDouble() ?: 0.0

internal fun FLDict.getBoolean(key: String): Boolean =
    FLValue_AsBool(FLDict_Get(this, key.toFLString()))

internal fun FLDict.getBlob(key: String): Blob? =
    getValue(key, false) as? Blob

internal fun FLDict.getDate(key: String): Instant? {
    val string = getValue(key, false) as? String ?: return null
    return try {
        Instant.parse(string)
    } catch (e: Throwable) {
        null
    }
}

internal fun Map<String, String>.toFLDict(): FLDict {
    return FLMutableDict_New()!!.apply {
        forEach { (key, value) ->
            FLMutableDict_SetString(this, key.toFLString(), value.toFLString())
        }
    }
}

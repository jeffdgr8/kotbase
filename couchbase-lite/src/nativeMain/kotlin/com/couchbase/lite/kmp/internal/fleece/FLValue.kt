package com.couchbase.lite.kmp.internal.fleece

import com.couchbase.lite.kmp.*
import com.couchbase.lite.kmp.CBLError
import kotlinx.cinterop.*
import libcblite.*

internal fun FLValue.toNative(isMutable: Boolean): Any? {
    return when (FLValue_GetType(this)) {
        kFLArray -> {
            val array = FLValue_AsArray(this)!!
            if (isMutable) {
                MutableArray(array)
            } else {
                Array(array)
            }
        }
        kFLDict -> {
            val dict = FLValue_AsDict(this)!!
            if (isMutable) {
                MutableDictionary(dict)
            } else {
                Dictionary(dict)
            }
        }
        kFLData -> Blob(content = FLValue_AsData(this))
        else -> toObject()
    }
}

internal fun FLValue.toObject(): Any? {
    return when (FLValue_GetType(this)) {
        kFLBoolean -> FLValue_AsBool(this)
        kFLNumber -> {
            when {
                FLValue_IsInteger(this) -> {
                    if (FLValue_IsUnsigned(this)) {
                        FLValue_AsUnsigned(this).toLong()
                    } else {
                        FLValue_AsInt(this)
                    }
                }
                FLValue_IsDouble(this) -> FLValue_AsDouble(this)
                else -> FLValue_AsFloat(this)
            }
        }
        kFLString -> FLValue_AsString(this).toKString()
        kFLData -> FLValue_AsData(this).toByteArray()
        kFLArray -> FLValue_AsArray(this)?.toList()
        kFLDict -> FLValue_AsDict(this)?.toMap()
        kFLNull -> null
        else -> null
    }
}

internal fun parseJson(json: String): Any? {
    val doc = wrapFLError { error ->
        memScoped {
            FLDoc_FromJSON(json.toFLString(this), error)
        }
    }
    return FLDoc_GetRoot(doc)?.toObject().also {
        FLDoc_Release(doc)
    }
}

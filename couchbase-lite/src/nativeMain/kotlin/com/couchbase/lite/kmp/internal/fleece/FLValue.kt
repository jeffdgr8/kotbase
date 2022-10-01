package com.couchbase.lite.kmp.internal.fleece

import com.couchbase.lite.kmp.*
import kotlinx.cinterop.*
import kotlinx.datetime.Instant
import libcblite.*

private inline val FLValue.type: FLValueType
    get() = FLValue_GetType(this)

internal fun FLValue.toNative(): Any? {
    return when (type) {
        kFLArray -> asArray()
        kFLDict -> {
            if (FLValue_IsBlob(this)) {
                asBlob()
            } else {
                asDictionary()
            }
        }
        kFLData -> asDataBlob()
        else -> toObject()
    }
}

internal fun FLValue.toMutableNative(saveMutableCopy: (Any) -> Unit): Any? {
    return when (type) {
        kFLArray -> asMutableArray(saveMutableCopy)
        kFLDict -> {
            if (FLValue_IsBlob(this)) {
                asBlob()
            } else {
                asMutableDictionary(saveMutableCopy)
            }
        }
        kFLData -> asDataBlob()
        else -> toObject()
    }
}

private fun FLValue.asArray(): Array =
    Array(FLValue_AsArray(this)!!)

private fun FLValue.asDictionary(): Dictionary =
    Dictionary(FLValue_AsDict(this)!!)

private fun FLValue.asMutableArray(saveMutableCopy: (MutableArray) -> Unit): MutableArray {
    val array = FLValue_AsArray(this)!!
    val mutableArray = FLArray_AsMutable(array)
    return if (mutableArray != null) {
        MutableArray(mutableArray)
    } else {
        MutableArray(FLArray_MutableCopy(array, kFLDefaultCopy)!!).also(saveMutableCopy)
    }
}

private fun FLValue.asMutableDictionary(saveMutableCopy: (MutableDictionary) -> Unit): MutableDictionary {
    val dict = FLValue_AsDict(this)!!
    val mutableDict = FLDict_AsMutable(dict)
    return if (mutableDict != null) {
        MutableDictionary(mutableDict)
    } else {
        MutableDictionary(FLDict_MutableCopy(dict, kFLDefaultCopy)!!).also(saveMutableCopy)
    }
}

private fun FLValue.asBlob(): Blob? =
    FLValue_GetBlob(this)?.asBlob()

private fun FLValue.asDataBlob(): Blob =
    Blob(content = FLValue_AsData(this))

internal fun FLValue.toArray(): Array? =
    if (type == kFLArray) asArray() else null

internal fun FLValue.toDictionary(): Dictionary? =
    if (type == kFLDict && !FLValue_IsBlob(this)) asDictionary() else null

internal fun FLValue.toMutableArray(saveMutableCopy: (MutableArray) -> Unit): MutableArray? =
    if (type == kFLArray) asMutableArray(saveMutableCopy) else null

internal fun FLValue.toMutableDictionary(saveMutableCopy: (MutableDictionary) -> Unit): MutableDictionary? =
    if (type == kFLDict && !FLValue_IsBlob(this)) asMutableDictionary(saveMutableCopy) else null

internal fun FLValue.toBlob(): Blob? {
    return when (type) {
        kFLDict -> asBlob()
        kFLData -> asDataBlob()
        else -> null
    }
}

internal fun FLValue.toObject(): Any? {
    return when (type) {
        kFLBoolean -> asBoolean()
        kFLNumber -> asNumber()
        kFLString -> asKString()
        kFLData -> FLValue_AsData(this).toByteArray()
        kFLArray -> FLValue_AsArray(this)?.toList()
        kFLDict -> FLValue_AsDict(this)?.toMap()
        kFLNull -> null
        else -> null
    }
}

private fun FLValue.asBoolean(): Boolean =
    FLValue_AsBool(this)

private fun FLValue.asNumber(): Number {
    return when {
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

private fun FLValue.asKString(): String? =
    FLValue_AsString(this).toKString()

internal fun FLValue?.toBoolean(): Boolean =
    FLValue_AsBool(this)

internal fun FLValue.toNumber(): Number? {
    return when (type) {
        kFLNumber -> asNumber()
        kFLBoolean -> if (asBoolean()) 1 else 0
        else -> null
    }
}

internal fun FLValue?.toInt(): Int =
    this?.toNumber()?.toInt() ?: 0

internal fun FLValue?.toLong(): Long =
    this?.toNumber()?.toLong() ?: 0L

internal fun FLValue?.toDouble(): Double =
    this?.toNumber()?.toDouble() ?: 0.0

internal fun FLValue?.toFloat(): Float =
    this?.toNumber()?.toFloat() ?: 0F

internal fun FLValue.toKString(): String? =
    if (type == kFLString) asKString() else null

internal fun FLValue.toDate(): Instant? {
    val string = toKString() ?: return null
    return try {
        Instant.parse(string)
    } catch (e: Throwable) {
        null
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

package com.couchbase.lite.kmp.internal.fleece

import com.couchbase.lite.kmp.*
import com.couchbase.lite.kmp.CBLError
import kotlinx.cinterop.*
import kotlinx.datetime.Instant
import libcblite.*

private inline val FLValue.type: FLValueType
    get() = FLValue_GetType(this)

internal fun FLValue.toNative(isMutable: Boolean): Any? {
    return when (type) {
        kFLArray -> asArray(isMutable)
        kFLDict -> {
            if (FLValue_IsBlob(this)) {
                asBlob()
            } else {
                asDictionary(isMutable)
            }
        }
        kFLData -> asDataBlob()
        else -> toObject()
    }
}

private fun FLValue.asArray(isMutable: Boolean): Array {
    val array = FLValue_AsArray(this)!!
    return if (isMutable) {
        MutableArray(array)
    } else {
        Array(array)
    }
}

private fun FLValue.asDictionary(isMutable: Boolean): Dictionary {
    val dict = FLValue_AsDict(this)!!
    return if (isMutable) {
        MutableDictionary(dict)
    } else {
        Dictionary(dict)
    }
}

private fun FLValue.asBlob(): Blob? =
    FLValue_GetBlob(this)?.asBlob()

private fun FLValue.asDataBlob(): Blob =
    Blob(content = FLValue_AsData(this))

internal fun FLValue.toArray(isMutable: Boolean): Array? =
    if (type == kFLArray) asArray(isMutable) else null

internal fun FLValue.toDictionary(isMutable: Boolean): Dictionary? =
    if (type == kFLDict) asDictionary(isMutable) else null

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

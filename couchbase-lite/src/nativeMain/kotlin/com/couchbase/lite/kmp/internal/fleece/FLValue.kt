package com.couchbase.lite.kmp.internal.fleece

import com.couchbase.lite.kmp.*
import com.couchbase.lite.kmp.internal.DbContext
import com.couchbase.lite.kmp.internal.wrapCBLError
import kotlinx.cinterop.*
import kotlinx.datetime.Instant
import libcblite.*

private inline val FLValue.type: FLValueType
    get() = FLValue_GetType(this)

internal fun FLValue.toNative(ctxt: DbContext?): Any? {
    return when (type) {
        kFLArray -> asArray(ctxt)
        kFLDict -> {
            if (FLValue_IsBlob(this)) {
                asBlob(ctxt)
            } else {
                asDictionary(ctxt)
            }
        }
        kFLData -> asDataBlob()
        else -> toObject(ctxt)
    }
}

internal fun FLValue.toMutableNative(ctxt: DbContext?, saveMutableCopy: (Any) -> Unit): Any? {
    return when (type) {
        kFLArray -> asMutableArray(ctxt, saveMutableCopy)
        kFLDict -> {
            if (FLValue_IsBlob(this)) {
                asBlob(ctxt)
            } else {
                asMutableDictionary(ctxt, saveMutableCopy)
            }
        }
        kFLData -> asDataBlob()
        else -> toObject(ctxt)
    }
}

private fun FLValue.asArray(ctxt: DbContext?): Array =
    Array(FLValue_AsArray(this)!!, ctxt)

private fun FLValue.asDictionary(ctxt: DbContext?): Dictionary =
    Dictionary(FLValue_AsDict(this)!!, ctxt)

private fun FLValue.asMutableArray(
    ctxt: DbContext?,
    saveMutableCopy: (MutableArray) -> Unit
): MutableArray {
    val array = FLValue_AsArray(this)!!
    val mutableArray = FLArray_AsMutable(array)
    return if (mutableArray != null) {
        MutableArray(mutableArray, ctxt)
    } else {
        MutableArray(FLArray_MutableCopy(array, kFLDefaultCopy)!!, ctxt)
            .also(saveMutableCopy)
    }
}

private fun FLValue.asMutableDictionary(
    ctxt: DbContext?,
    saveMutableCopy: (MutableDictionary) -> Unit
): MutableDictionary {
    val dict = FLValue_AsDict(this)!!
    val mutableDict = FLDict_AsMutable(dict)
    return if (mutableDict != null) {
        MutableDictionary(mutableDict, ctxt)
    } else {
        MutableDictionary(FLDict_MutableCopy(dict, kFLDefaultCopy)!!, ctxt)
            .also(saveMutableCopy)
    }
}

private fun FLValue.asBlob(ctxt: DbContext?): Blob? {
    val dict = FLValue_AsDict(this)
    val db = ctxt?.database
    if (db != null) {
        val dbBlob = wrapCBLError { error ->
            CBLDatabase_GetBlob(db.actual, dict, error)
        }
        if (dbBlob != null) {
            return Blob(dbBlob, ctxt)
        }
    }
    return FLValue_GetBlob(this)?.asBlob(ctxt)
}

private fun FLValue.asDataBlob(): Blob =
    Blob(content = FLValue_AsData(this))

internal fun FLValue.toArray(ctxt: DbContext?): Array? =
    if (type == kFLArray) asArray(ctxt) else null

internal fun FLValue.toDictionary(ctxt: DbContext?): Dictionary? =
    if (type == kFLDict && !FLValue_IsBlob(this)) asDictionary(ctxt) else null

internal fun FLValue.toMutableArray(
    ctxt: DbContext?,
    saveMutableCopy: (MutableArray) -> Unit
): MutableArray? {
    return if (type == kFLArray) {
        asMutableArray(ctxt, saveMutableCopy)
    } else null
}

internal fun FLValue.toMutableDictionary(
    ctxt: DbContext?,
    saveMutableCopy: (MutableDictionary) -> Unit
): MutableDictionary? {
    return if (type == kFLDict && !FLValue_IsBlob(this)) {
        asMutableDictionary(ctxt, saveMutableCopy)
    } else null
}

internal fun FLValue.toBlob(ctxt: DbContext?): Blob? {
    return when (type) {
        kFLDict -> asBlob(ctxt)
        kFLData -> asDataBlob()
        else -> null
    }
}

internal fun FLValue.toObject(ctxt: DbContext?, blobDictAsBlob: Boolean = true): Any? {
    return when (type) {
        kFLBoolean -> asBoolean()
        kFLNumber -> asNumber()
        kFLString -> asKString()
        kFLData -> FLValue_AsData(this).toByteArray()
        kFLArray -> FLValue_AsArray(this)?.toList(ctxt)
        kFLDict -> {
            if (blobDictAsBlob && FLValue_IsBlob(this)) {
                asBlob(ctxt)
            } else {
                FLValue_AsDict(this)?.toMap(ctxt)
            }
        }
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
    return FLDoc_GetRoot(doc)?.toObject(null, false).also {
        FLDoc_Release(doc)
    }
}

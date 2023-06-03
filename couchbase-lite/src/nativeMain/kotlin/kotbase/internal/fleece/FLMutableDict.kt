package kotbase.internal.fleece

import kotbase.*
import kotbase.ext.toStringMillis
import kotbase.internal.DbContext
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import libcblite.*

internal fun FLMutableDict.setValue(key: String, value: Any?, ctxt: DbContext?) {
    @Suppress("UNCHECKED_CAST")
    when (value) {
        is Boolean -> setBoolean(key, value)
        is ByteArray -> setBlob(key, Blob(value), ctxt)
        is Blob -> setBlob(key, value, ctxt)
        is String -> setString(key, value)
        is Instant -> setDate(key, value)
        is Number -> setNumber(key, value)
        is List<*> -> setArray(key, MutableArray(value), ctxt)
        is Array -> setArray(key, value, ctxt)
        is Map<*, *> -> setDictionary(key, MutableDictionary(value as Map<String, Any?>), ctxt)
        is Dictionary -> setDictionary(key, value, ctxt)
        null -> FLMutableDict_SetNull(this, key.toFLString())
        else -> invalidTypeError(value)
    }
}

internal fun FLMutableDict.setString(key: String, value: String?) {
    if (value != null) {
        FLMutableDict_SetString(this, key.toFLString(), value.toFLString())
    } else {
        FLMutableDict_SetNull(this, key.toFLString())
    }
}

internal fun FLMutableDict.setNumber(key: String, value: Number?) {
    when (value) {
        is Double -> FLMutableDict_SetDouble(this, key.toFLString(), value)
        is Float -> FLMutableDict_SetFloat(this, key.toFLString(), value)
        null -> FLMutableDict_SetNull(this, key.toFLString())
        else -> FLMutableDict_SetInt(this, key.toFLString(), value.toLong().convert())
    }
}

internal fun FLMutableDict.setInt(key: String, value: Int) {
    FLMutableDict_SetInt(this, key.toFLString(), value.convert())
}

internal fun FLMutableDict.setLong(key: String, value: Long) {
    FLMutableDict_SetInt(this, key.toFLString(), value.convert())
}

internal fun FLMutableDict.setFloat(key: String, value: Float) {
    FLMutableDict_SetFloat(this, key.toFLString(), value)
}

internal fun FLMutableDict.setDouble(key: String, value: Double) {
    FLMutableDict_SetDouble(this, key.toFLString(), value)
}

internal fun FLMutableDict.setBoolean(key: String, value: Boolean) {
    FLMutableDict_SetBool(this, key.toFLString(), value)
}

internal fun FLMutableDict.setBlob(key: String, value: Blob?, ctxt: DbContext?) {
    if (value?.actual == null) {
        FLMutableDict_SetNull(this, key.toFLString())
    } else {
        FLMutableDict_SetBlob(this, key.toFLString(), value.actual)
    }
    value?.checkSetDb(ctxt)
}

internal fun FLMutableDict.setDate(key: String, value: Instant?) {
    if (value != null) {
        FLMutableDict_SetString(this, key.toFLString(), value.toStringMillis().toFLString())
    } else {
        FLMutableDict_SetNull(this, key.toFLString())
    }
}

internal fun FLMutableDict.setArray(key: String, value: Array?, ctxt: DbContext?) {
    if (value != null) {
        value.dbContext = ctxt
        FLMutableDict_SetArray(this, key.toFLString(), value.actual)
    } else {
        FLMutableDict_SetNull(this, key.toFLString())
    }
}

internal fun FLMutableDict.setDictionary(key: String, value: Dictionary?, ctxt: DbContext?) {
    if (value != null) {
        checkSelf(value.actual)
        value.dbContext = ctxt
        FLMutableDict_SetDict(this, key.toFLString(), value.actual)
    } else {
        FLMutableDict_SetNull(this, key.toFLString())
    }
}

private fun FLMutableDict.checkSelf(value: FLMutableDict) {
    if (value === this) {
        throw IllegalArgumentException("Dictionaries cannot ba added to themselves")
    }
}

internal fun FLMutableDict.remove(key: String) {
    FLMutableDict_Remove(this, key.toFLString())
}

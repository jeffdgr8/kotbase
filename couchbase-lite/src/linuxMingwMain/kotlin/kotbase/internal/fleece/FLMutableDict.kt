/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase.internal.fleece

import kotbase.*
import kotbase.ext.toStringMillis
import kotbase.internal.DbContext
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
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
        null -> memScoped {
            FLMutableDict_SetNull(this@setValue, key.toFLString(this))
        }
        else -> invalidTypeError(value)
    }
}

internal fun FLMutableDict.setString(key: String, value: String?) {
    memScoped {
        if (value != null) {
            FLMutableDict_SetString(
                this@setString,
                key.toFLString(this),
                value.toFLString(this)
            )
        } else {
            FLMutableDict_SetNull(this@setString, key.toFLString(this))
        }
    }
}

internal fun FLMutableDict.setNumber(key: String, value: Number?) {
    memScoped {
        when (value) {
            is Double -> FLMutableDict_SetDouble(this@setNumber, key.toFLString(this), value)
            is Float -> FLMutableDict_SetFloat(this@setNumber, key.toFLString(this), value)
            null -> FLMutableDict_SetNull(this@setNumber, key.toFLString(this))
            else -> FLMutableDict_SetInt(this@setNumber, key.toFLString(this), value.toLong().convert())
        }
    }
}

internal fun FLMutableDict.setInt(key: String, value: Int) {
    memScoped {
        FLMutableDict_SetInt(this@setInt, key.toFLString(this), value.convert())
    }
}

internal fun FLMutableDict.setLong(key: String, value: Long) {
    memScoped {
        FLMutableDict_SetInt(this@setLong, key.toFLString(this), value.convert())
    }
}

internal fun FLMutableDict.setFloat(key: String, value: Float) {
    memScoped {
        FLMutableDict_SetFloat(this@setFloat, key.toFLString(this), value)
    }
}

internal fun FLMutableDict.setDouble(key: String, value: Double) {
    memScoped {
        FLMutableDict_SetDouble(this@setDouble, key.toFLString(this), value)
    }
}

internal fun FLMutableDict.setBoolean(key: String, value: Boolean) {
    memScoped {
        FLMutableDict_SetBool(this@setBoolean, key.toFLString(this), value)
    }
}

internal fun FLMutableDict.setBlob(key: String, value: Blob?, ctxt: DbContext?) {
    memScoped {
        if (value?.actual == null) {
            FLMutableDict_SetNull(this@setBlob, key.toFLString(this))
        } else {
            FLMutableDict_SetBlob(this@setBlob, key.toFLString(this), value.actual)
        }
    }
    value?.checkSetDb(ctxt)
}

internal fun FLMutableDict.setDate(key: String, value: Instant?) {
    memScoped {
        if (value != null) {
            FLMutableDict_SetString(
                this@setDate,
                key.toFLString(this),
                value.toStringMillis().toFLString(this)
            )
        } else {
            FLMutableDict_SetNull(this@setDate, key.toFLString(this))
        }
    }
}

internal fun FLMutableDict.setArray(key: String, value: Array?, ctxt: DbContext?) {
    memScoped {
        if (value != null) {
            value.dbContext = ctxt
            FLMutableDict_SetArray(this@setArray, key.toFLString(this), value.actual)
        } else {
            FLMutableDict_SetNull(this@setArray, key.toFLString(this))
        }
    }
}

internal fun FLMutableDict.setDictionary(key: String, value: Dictionary?, ctxt: DbContext?) {
    memScoped {
        if (value != null) {
            checkSelf(value.actual)
            value.dbContext = ctxt
            FLMutableDict_SetDict(this@setDictionary, key.toFLString(this), value.actual)
        } else {
            FLMutableDict_SetNull(this@setDictionary, key.toFLString(this))
        }
    }
}

private fun FLMutableDict.checkSelf(value: FLMutableDict) {
    if (value === this) {
        throw IllegalArgumentException("Dictionaries cannot ba added to themselves")
    }
}

internal fun FLMutableDict.remove(key: String) {
    memScoped {
        FLMutableDict_Remove(this@remove, key.toFLString(this))
    }
}

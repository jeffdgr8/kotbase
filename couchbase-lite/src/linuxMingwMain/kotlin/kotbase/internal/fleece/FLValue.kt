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
import kotbase.CBLError
import kotbase.internal.DbContext
import kotbase.internal.wrapCBLError
import kotlin.time.Instant
import libcblite.*

private inline val FLValue.type: FLValueType
    get() = FLValue_GetType(this)

internal fun FLValue.toNative(ctxt: DbContext?, release: Boolean = true): Any? = when (type) {
    kFLArray -> asArray(ctxt, release)
    kFLDict -> {
        if (FLValue_IsBlob(this)) {
            asBlob(ctxt, release)
        } else {
            asDictionary(ctxt, release)
        }
    }
    kFLData -> asDataBlob()
    else -> toObject(ctxt)
}

internal fun FLValue.toMutableNative(ctxt: DbContext?, saveMutableCopy: (Any) -> Unit): Any? = when (type) {
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

private fun FLValue.asArray(ctxt: DbContext?, release: Boolean = true): Array =
    Array(FLValue_AsArray(this)!!, ctxt, release = release)

private fun FLValue.asDictionary(ctxt: DbContext?, release: Boolean = true): Dictionary =
    Dictionary(FLValue_AsDict(this)!!, ctxt, release = release)

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

private fun FLValue.asBlob(ctxt: DbContext?, release: Boolean = true): Blob? {
    if (!FLValue_IsBlob(this)) return null
    val db = ctxt?.database
    if (db != null) {
        val dbBlob = try {
            wrapCBLError { error ->
                CBLDatabase_GetBlob(db.actual, FLValue_AsDict(this), error)
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code == CBLError.Code.NOT_OPEN && e.domain == CBLError.Domain.CBLITE) {
                return Blob(
                    // database is closed, just use blob dictionary, content won't be available
                    Dictionary(FLValue_AsDict(this)!!, null, release = release),
                    ctxt
                )
            } else {
                throw e
            }
        }
        if (dbBlob != null) {
            return Blob(dbBlob, ctxt)
        }
    }
    return FLValue_GetBlob(this)?.asBlob(ctxt)
        ?: Blob(
            // last resort if still null, just use blob dictionary, content won't be available
            Dictionary(FLValue_AsDict(this)!!, null, release = release)
        )
}

private fun FLValue.asDataBlob(): Blob =
    Blob(content = FLValue_AsData(this))

internal fun FLValue.toArray(ctxt: DbContext?, release: Boolean = true): Array? =
    if (type == kFLArray) asArray(ctxt, release = release) else null

internal fun FLValue.toDictionary(ctxt: DbContext?, release: Boolean = true): Dictionary? =
    if (type == kFLDict && !FLValue_IsBlob(this)) asDictionary(ctxt, release = release) else null

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

internal fun FLValue.toBlob(ctxt: DbContext?, release: Boolean = true): Blob? = when (type) {
    kFLDict -> asBlob(ctxt, release = release)
    kFLData -> asDataBlob()
    else -> null
}

internal fun FLValue.toObject(ctxt: DbContext?, blobDictAsBlob: Boolean = true): Any? = when (type) {
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

private fun FLValue.asBoolean(): Boolean =
    FLValue_AsBool(this)

private fun FLValue.asNumber(): Number = when {
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

private fun FLValue.asKString(): String? =
    FLValue_AsString(this).toKString()

internal fun FLValue?.toBoolean(): Boolean =
    FLValue_AsBool(this)

internal fun FLValue.toNumber(): Number? = when (type) {
    kFLNumber -> asNumber()
    kFLBoolean -> if (asBoolean()) 1 else 0
    else -> null
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

package com.couchbase.lite.kmp.ext

import com.couchbase.lite.kmp.CouchbaseLiteException
import com.udobny.kmp.AbstractDelegatedClass
import com.udobny.kmp.ext.wrapError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import platform.Foundation.NSError
import platform.darwin.NSObject

@Suppress("UNCHECKED_CAST")
internal fun NSError.toCouchbaseLiteException(): CouchbaseLiteException =
    CouchbaseLiteException(
        localizedDescription,
        null,
        domain,
        code.toInt(),
        userInfo as Map<String, Any?>?
    )

@Throws(CouchbaseLiteException::class)
internal fun <D : NSObject, R> AbstractDelegatedClass<D>.wrapCBLError(action: D.(error: CPointer<ObjCObjectVar<NSError?>>) -> R): R {
    return wrapError(NSError::toCouchbaseLiteException) { error ->
        actual.action(error)
    }
}

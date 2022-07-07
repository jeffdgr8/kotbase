package com.couchbase.lite.kmm.ext

import com.couchbase.lite.kmm.CouchbaseLiteException
import com.udobny.kmm.AbstractDelegatedClass
import com.udobny.kmm.ext.wrapError
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
internal fun <D : NSObject, R> AbstractDelegatedClass<D>.throwError(action: D.(error: CPointer<ObjCObjectVar<NSError?>>) -> R): R {
    return wrapError(NSError::toCouchbaseLiteException) { error ->
        actual.action(error)
    }
}

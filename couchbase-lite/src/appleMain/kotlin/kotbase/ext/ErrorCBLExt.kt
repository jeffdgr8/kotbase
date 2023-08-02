package kotbase.ext

import kotbase.CouchbaseLiteException
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import platform.Foundation.NSError

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
internal fun <R> wrapCBLError(action: (error: CPointer<ObjCObjectVar<NSError?>>) -> R): R =
    wrapError(NSError::toCouchbaseLiteException) { error ->
        action(error)
    }

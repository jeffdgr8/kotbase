package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLBlob
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toByteArray
import com.udobny.kmm.ext.toNSData
import com.udobny.kmm.ext.wrapError
import okio.Source
import okio.buffer
import platform.Foundation.NSError
import platform.Foundation.NSURL

public actual class Blob
internal constructor(actual: CBLBlob) : DelegatedClass<CBLBlob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        CBLBlob(contentType, content.toNSData())
    )

    // TODO: https://github.com/square/okio/pull/1123
    //public actual constructor(contentType: String, stream: Source) : this(
    //    CBLBlob(contentType, stream.buffer().source())
    //)

    @Throws(CouchbaseLiteException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            CBLBlob(contentType, NSURL(string = fileURL), error)
        }
    )

    public actual fun getContent(): ByteArray? =
        actual.content?.toByteArray()

    // TODO: https://github.com/square/okio/pull/1123
    //public actual fun getContentStream(): Source? =
    //    actual.contentStream?.source()

    public actual fun getContentType(): String =
        actual.contentType!!

    public actual fun length(): Long =
        actual.length.toLong()

    public actual fun digest(): String? =
        actual.digest

    @Suppress("UNCHECKED_CAST")
    public actual fun getProperties(): Map<String, Any?> =
        actual.properties as Map<String, Any?>
}

internal fun CBLBlob.asBlob() = Blob(this)

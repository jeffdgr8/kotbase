package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import okio.Source
import okio.buffer
import okio.source
import java.io.IOException
import java.net.URL

public actual class Blob
internal constructor(actual: com.couchbase.lite.Blob) :
    DelegatedClass<com.couchbase.lite.Blob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        com.couchbase.lite.Blob(contentType, content)
    )

    // TODO: https://github.com/square/okio/pull/1123
    //public actual constructor(contentType: String, stream: Source) : this(
    //    com.couchbase.lite.Blob(contentType, stream.buffer().inputStream())
    //)

    @Throws(CouchbaseLiteException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        try {
            com.couchbase.lite.Blob(contentType, URL(fileURL))
        } catch (e: IOException) {
            throw CouchbaseLiteException(e.message, e, null, 0, null)
        }
    )

    public actual fun getContent(): ByteArray? =
        actual.content

    // TODO: https://github.com/square/okio/pull/1123
    //public actual fun getContentStream(): Source? =
    //    actual.contentStream?.source()

    public actual fun getContentType(): String =
        actual.contentType

    public actual fun length(): Long =
        actual.length()

    public actual fun digest(): String? =
        actual.digest()

    @Suppress("UNCHECKED_CAST")
    public actual fun getProperties(): Map<String, Any?> =
        actual.properties
}

internal fun com.couchbase.lite.Blob.asBlob() = Blob(this)

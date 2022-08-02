package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import okio.Source
import okio.buffer
import okio.source
import java.net.MalformedURLException
import java.net.URL

public actual class Blob
internal constructor(actual: com.couchbase.lite.Blob) :
    DelegatedClass<com.couchbase.lite.Blob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        com.couchbase.lite.Blob(contentType, content)
    )

    public actual constructor(contentType: String, stream: Source) : this(
        com.couchbase.lite.Blob(contentType, stream.buffer().inputStream())
    )

    @Throws(CouchbaseLiteException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        com.couchbase.lite.Blob(contentType, fileURL.toFileUrl())
    )

    public actual val content: ByteArray?
        get() = actual.content

    public actual val contentStream: Source?
        get() = actual.contentStream?.source()

    public actual val contentType: String
        get() = actual.contentType

    public actual fun toJSON(): String =
        actual.toJSON()

    public actual val length: Long
        get() = actual.length()

    public actual val digest: String?
        get() = actual.digest()

    public actual val properties: Map<String, Any?>
        get() = actual.properties

    public actual companion object {

        public actual fun isBlob(props: Map<String, Any?>?): Boolean =
            com.couchbase.lite.Blob.isBlob(props)
    }
}

internal fun com.couchbase.lite.Blob.asBlob() = Blob(this)

private fun String.toFileUrl(): URL {
    return try {
        URL(this)
    } catch (e: MalformedURLException) {
        URL("file://$this")
    }
}

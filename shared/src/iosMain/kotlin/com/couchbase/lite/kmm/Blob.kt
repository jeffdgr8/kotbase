package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLBlob
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toByteArray
import com.udobny.kmm.ext.toNSData

public actual class Blob
internal constructor(actual: CBLBlob) : DelegatedClass<CBLBlob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        CBLBlob(contentType, content.toNSData())
    )

    //TODO: use https://github.com/Kotlin/kotlinx-io
    //constructor(contentType: String, stream: InputStream)

    // TODO:
    //@Throws(IOException::class)
    //constructor(contentType: String, fileURL: URL)

    public actual fun getContent(): ByteArray? =
        actual.content?.toByteArray()

    //TODO: use https://github.com/Kotlin/kotlinx-io
    //public actual fun getContentStream(): InputStream?

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

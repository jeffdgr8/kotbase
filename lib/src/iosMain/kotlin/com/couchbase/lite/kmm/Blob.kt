package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLBlob
import cocoapods.CouchbaseLite.CBLDatabase
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toByteArray
import com.udobny.kmm.ext.toNSData
import com.udobny.kmm.ext.wrapError
import okio.Source
import okio.buffer
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.valueForKey

public actual class Blob
internal constructor(actual: CBLBlob) : DelegatedClass<CBLBlob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        CBLBlob(contentType, content.toNSData())
    )

    // TODO: https://github.com/square/okio/pull/1123
    //public actual constructor(contentType: String, stream: Source) : this(
    //    CBLBlob(contentType, stream.buffer().inputStream())
    //)

    @Throws(CouchbaseLiteException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            CBLBlob(contentType, NSURL(fileURLWithPath = fileURL), error)
        }
    )

    public actual val content: ByteArray?
        get() {
            val content = actual.content?.toByteArray()
            if (content == null) {
                val db = Database(actual.valueForKey("_db") as CBLDatabase)
                if (!db.isOpen) {
                    // Java SDK throws exception rather than just returning null when database is closed
                    throw IllegalStateException("Failed to read content from database for digest: $digest")
                }
            }
            return content
        }

    // TODO: https://github.com/square/okio/pull/1123
    //public actual val contentStream: Source?
    //    get() = actual.contentStream?.source()

    public actual val contentType: String
        get() = actual.contentType!!

    public actual val length: Long
        get() = actual.length.toLong()

    public actual val digest: String?
        get() = actual.digest

    @Suppress("UNCHECKED_CAST")
    public actual val properties: Map<String, Any?>
        get() = actual.properties as Map<String, Any?>
}

internal fun CBLBlob.asBlob() = Blob(this)

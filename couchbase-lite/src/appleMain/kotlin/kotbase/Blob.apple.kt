package kotbase

import cocoapods.CouchbaseLite.CBLBlob
import cocoapods.CouchbaseLite.CBLDatabase
import kotbase.base.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toNSData
import kotbase.ext.wrapCBLError
import okio.FileNotFoundException
import okio.IOException
import okio.Source
import okio.buffer
import okio.ext.inputStream
import okio.ext.source
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.valueForKey

public actual class Blob
internal constructor(actual: CBLBlob) : DelegatedClass<CBLBlob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        CBLBlob(contentType, content.toNSData())
    )

    public actual constructor(contentType: String, stream: Source) : this(
        CBLBlob(contentType, stream.buffer().inputStream())
    )

    @Throws(IOException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        wrapCBLError { error ->
            val url = fileURL.toFileUrl()
            if (!NSFileManager.defaultManager.fileExistsAtPath(url.path!!)) {
                throw FileNotFoundException("${url.path}: open failed: ENOENT (No such file or directory)")
            }
            CBLBlob(contentType, url, error)
        }
    )

    public actual val content: ByteArray?
        get() {
            val content = actual.content?.toByteArray()
            if (content == null) {
                val cblDb = actual.valueForKey("_db") as CBLDatabase?
                if (cblDb != null) {
                    // Java SDK throws exception rather than just returning null when database is closed
                    Database(cblDb).mustBeOpen()
                }
            }
            return content
        }

    public actual val contentStream: Source?
        get() = actual.contentStream?.source()

    public actual val contentType: String
        get() = actual.contentType!!

    public actual fun toJSON(): String {
        if (digest == null) {
            throw IllegalStateException("A Blob may be encoded as JSON only after it has been saved in a database")
        }
        return actual.toJSON()
    }

    public actual val length: Long
        get() = actual.length.toLong()

    public actual val digest: String?
        get() = actual.digest

    @Suppress("UNCHECKED_CAST")
    public actual val properties: Map<String, Any?>
        get() = actual.properties as Map<String, Any?>

    public actual companion object {

        public actual fun isBlob(props: Map<String, Any?>?): Boolean {
            props ?: return false
            @Suppress("UNCHECKED_CAST")
            return CBLBlob.isBlob(props as Map<Any?, *>)
        }
    }
}

internal fun CBLBlob.asBlob() = Blob(this)

private fun String.toFileUrl(): NSURL {
    return NSURL(string = this).let {
        if (it.scheme.equals("file", ignoreCase = true)) {
            it
        } else if (it.scheme == null) {
            NSURL(fileURLWithPath = this)
        } else {
            throw IllegalArgumentException("$this must be a file-based URL.")
        }
    }
}
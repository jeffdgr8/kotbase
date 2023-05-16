package com.couchbase.lite.kmp

import cnames.structs.CBLBlob
import com.couchbase.lite.kmp.internal.DbContext
import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.wrapCBLError
import com.udobny.kmp.identityHashCodeHex
import kotlinx.cinterop.*
import libcblite.*
import okio.*
import okio.Path.Companion.toPath
import platform.posix.EINVAL
import platform.posix.R_OK
import platform.posix.access
import platform.posix.errno
import kotlin.native.internal.createCleaner

private const val MIME_UNKNOWN = "application/octet-stream"

public actual class Blob
private constructor(
    actual: CPointer<CBLBlob>?,
    dbContext: DbContext? = null,
    private val dict: Dictionary? = null
) {

    init {
        CBLBlob_Retain(actual)
    }

    private val memory = object {
        var actual: CPointer<CBLBlob>? = actual
    }

    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLBlob_Release(it.actual)
    }

    public actual constructor(contentType: String, content: ByteArray) : this(
        contentType.toFLString(),
        content.toFLSlice()
    ) {
        blobContent = content
    }

    internal constructor(content: ByteArray) : this(MIME_UNKNOWN, content)

    internal constructor(
        contentType: CValue<FLString> = MIME_UNKNOWN.toFLString(),
        content: CValue<FLSlice>
    ) : this(CBLBlob_CreateWithData(contentType, content)!!) {
        CBLBlob_Release(actual)
    }

    internal constructor(actual: CPointer<CBLBlob>?, dbContext: DbContext? = null) :
            this(actual, dbContext, null)

    internal constructor(dict: Dictionary?) : this(actual = null, dict = dict)

    public actual constructor(contentType: String, stream: Source) : this(actual = null) {
        blobContentType = contentType
        blobContentStream = stream
    }

    @Throws(IOException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        contentType,
        fileURL.toFileSource()
    )

    internal val actual: CPointer<CBLBlob>?
        get() = memory.actual

    private var dbContext: DbContext? = dbContext
        set(value) {
            field = value
            val db = value?.database
            if (db != null && actual == null) {
                saveToDb(db)
            } else {
                value?.addStreamBlob(this)
            }
        }

    internal fun saveToDb(db: Database) {
        if (actual == null) {
            memory.actual = CBLBlob_CreateWithStream(
                blobContentType.toFLString(),
                blobContentStream!!.blobWriteStream(db)
            )
        }
    }

    private var blobContent: ByteArray? = null

    public actual val content: ByteArray?
        get() {
            if (blobContent == null) {
                if (blobContentStream != null) {
                    blobContentStream!!.buffer().use {
                        blobContent = it.readByteArray()
                    }
                    blobContentStream = null
                    memory.actual = CBLBlob_CreateWithData(
                        blobContentType.toFLString(),
                        blobContent!!.toFLSlice()
                    )
                } else if (actual != null) {
                    dbContext?.database?.mustBeOpen()
                    blobContent = wrapCBLError { error ->
                        CBLBlob_Content(actual, error).toByteArray()
                    }
                }
            }
            return blobContent?.copyOf()
        }

    private var blobContentStream: Source? = null

    public actual val contentStream: Source?
        get() {
            if (blobContent != null) {
                return Buffer().apply {
                    write(blobContent!!)
                }
            }
            actual ?: return null
            return wrapCBLError { error ->
                CBLBlob_OpenContentStream(actual, error)?.source()
            }
        }

    private var blobContentType: String? = null

    public actual val contentType: String
        get() {
            return if (actual != null) {
                CBLBlob_ContentType(actual).toKString()
            } else {
                dict?.getString(PROP_CONTENT_TYPE)
            } ?: blobContentType ?: MIME_UNKNOWN
        }

    public actual fun toJSON(): String {
        if (digest == null) {
            throw IllegalStateException("A Blob may be encoded as JSON only after it has been saved in a database")
        }
        return if (actual != null) {
            FLValue_ToJSON(CBLBlob_Properties(actual)?.reinterpret()).toKString()!!
        } else {
            dict!!.toJSON()
        }
    }

    public actual val length: Long
        get() {
            return if (actual != null) CBLBlob_Length(actual).toLong()
            else dict?.getLong(PROP_LENGTH) ?: 0
        }

    public actual val digest: String?
        get() {
            // Java SDK sets digest only after installed in database
            return if (dbContext?.database == null) null
            else if (actual != null) CBLBlob_Digest(actual).toKString()
            else dict?.getString(PROP_DIGEST)
        }

    public actual val properties: Map<String, Any?>
        get() = CBLBlob_Properties(actual)!!.toMap(null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Blob) return false
        return if (actual != null) CBLBlob_Equals(actual, other.actual)
        else if (blobContentStream != null) blobContentStream == other.blobContentStream
        else dict == other.dict
    }

    override fun hashCode(): Int =
        content.contentHashCode()

    override fun toString(): String =
        "Blob{${identityHashCodeHex()}: $digest($contentType, $length)}"

    internal fun checkSetDb(dbContext: DbContext?) {
        if (this.dbContext == null) {
            this.dbContext = dbContext
        } else if (dbContext?.database == null && this.dbContext?.database != null) {
            dbContext?.database = this.dbContext?.database
        }
    }

    public actual companion object {

        private const val META_PROP_TYPE = "@type"
        private const val TYPE_BLOB = "blob"

        private const val PROP_DIGEST = "digest"
        private const val PROP_LENGTH = "length"
        private const val PROP_CONTENT_TYPE = "content_type"

        public actual fun isBlob(props: Map<String, Any?>?): Boolean {
            props ?: return false

            // Java SDK check is stricter than C SDK, which only checks @type = blob
            //return FLDict_IsBlob(MutableDictionary(props).actual)

            if (props[PROP_DIGEST] !is String) return false
            if (TYPE_BLOB != props[META_PROP_TYPE]) return false
            var nProps = 2

            if (props.containsKey(PROP_CONTENT_TYPE)) {
                if (props[PROP_CONTENT_TYPE] !is String) return false
                nProps++
            }

            val len = props[PROP_LENGTH]
            if (len != null) {
                if (len !is Int && len !is Long) return false
                nProps++
            }

            return nProps == props.size
        }
    }
}

internal fun CPointer<CBLBlob>.asBlob(ctxt: DbContext?) = Blob(this, ctxt)

private fun CPointer<CBLBlobReadStream>.source(): Source =
    BlobReadStreamSource(this)

private class BlobReadStreamSource(val actual: CPointer<CBLBlobReadStream>) : Source {

    private val memory = object {
        var closeCalled = false
        val actual = this@BlobReadStreamSource.actual
    }

    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLBlobReader_Close(it.actual)
        }
    }

    override fun close() {
        memory.closeCalled = true
        CBLBlobReader_Close(actual)
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (byteCount == 0L) return 0L
        require(byteCount >= 0L) { "byteCount < 0: $byteCount" }
        val bytes = ByteArray(byteCount.toInt())
        val bytesRead = wrapCBLError { error ->
            CBLBlobReader_Read(actual, bytes.refTo(0), byteCount.convert(), error)
        }
        if (bytesRead < 0) throw IOException()
        if (bytesRead == 0) return -1
        sink.write(bytes, 0, bytesRead)
        return bytesRead.toLong()
    }

    override fun timeout(): Timeout = Timeout.NONE
}

private fun Source.blobWriteStream(db: Database): CPointer<CBLBlobWriteStream> {
    val writer = wrapCBLError { error ->
        CBLBlobWriter_Create(db.actual, error)!!
    }
    try {
        val bufferSize = 8 * 1024
        val buffer = ByteArray(bufferSize)
        buffer().use { source ->
            while (!source.exhausted()) {
                val read = source.read(buffer)
                wrapCBLError { error ->
                    CBLBlobWriter_Write(writer, buffer.refTo(0), read.convert(), error)
                }
            }
        }
    } catch (e: Exception) {
        CBLBlobWriter_Close(writer)
        throw e
    }
    return writer
}

private fun String.toFileSource(): Source {
    val path = toFilePath()
    val fs = FileSystem.SYSTEM
    if (!fs.exists(path)) {
        throw FileNotFoundException("$this: open failed: ENOENT (No such file or directory)")
    }
    return fs.source(path)
}

private fun String.toFilePath(): Path {
    val match = """^(?:([a-zA-Z][a-zA-Z0-9+.-]*):)?.+$""".toRegex()
        .matchEntire(this)
    match?.groups?.get(1)?.let { scheme ->
        // Windows drive letters are ok
        if (Platform.osFamily != OsFamily.WINDOWS || scheme.value.length != 1) {
            if (!scheme.value.equals("file", ignoreCase = true)) {
                throw IllegalArgumentException("$this must be a file-based URL.")
            }
        }
    }
    if (access(this, R_OK) == -1 && errno == EINVAL) {
        throw IllegalArgumentException("$this must be a valid file path.")
    }
    return toPath()
}

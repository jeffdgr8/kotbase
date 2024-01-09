/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import cnames.structs.CBLBlob
import cnames.structs.CBLBlobReadStream
import cnames.structs.CBLBlobWriteStream
import kotbase.internal.DbContext
import kotbase.internal.fleece.*
import kotbase.internal.wrapCBLError
import kotbase.util.identityHashCodeHex
import kotlinx.cinterop.*
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import libcblite.*
import platform.posix.EINVAL
import platform.posix.R_OK
import platform.posix.access
import platform.posix.errno
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

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
        var actual = actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLBlob_Release(it.actual)
    }

    public actual constructor(contentType: String, content: ByteArray) : this(
        contentType,
        content.toFLSlice()
    ) {
        blobContent = content
    }

    internal constructor(content: ByteArray) : this(MIME_UNKNOWN, content)

    internal constructor(
        contentType: String = MIME_UNKNOWN,
        content: CValue<FLSlice>
    ) : this(
        memScoped {
            CBLBlob_CreateWithData(contentType.toFLString(this), content)!!
        }
    ) {
        CBLBlob_Release(actual)
    }

    internal constructor(actual: CPointer<CBLBlob>?, dbContext: DbContext? = null) :
            this(actual, dbContext, null)

    internal constructor(dict: Dictionary?, ctxt: DbContext? = null) :
            this(actual = null, dbContext = ctxt, dict = dict)

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
            memScoped {
                memory.actual = CBLBlob_CreateWithStream(
                    blobContentType.toFLString(this),
                    blobContentStream!!.blobWriteStream(db)
                )
            }
        }
    }

    private var blobContent: ByteArray? = null

    @OptIn(ExperimentalStdlibApi::class)
    public actual val content: ByteArray?
        get() {
            if (blobContent == null) {
                if (blobContentStream != null) {
                    blobContentStream!!.use {
                        blobContent = it.readByteArray()
                    }
                    blobContentStream = null
                    memScoped {
                        memory.actual = CBLBlob_CreateWithData(
                            blobContentType.toFLString(this),
                            blobContent!!.toFLSlice()
                        )
                    }
                } else {
                    dbContext?.database?.mustBeOpen()
                    if (actual != null) {
                        blobContent = wrapCBLError { error ->
                            CBLBlob_Content(actual, error).toByteArray()
                        }
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
                CBLBlob_OpenContentStream(actual, error)?.asSource()?.buffered()
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

private fun CPointer<CBLBlobReadStream>.asSource(): RawSource =
    BlobReadStreamSource(this)

private class BlobReadStreamSource(actual: CPointer<CBLBlobReadStream>) : RawSource {

    private val memory = object {
        var closeCalled = false
        val actual = actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLBlobReader_Close(it.actual)
        }
    }

    val actual: CPointer<CBLBlobReadStream>
        get() = memory.actual

    override fun close() {
        memory.closeCalled = true
        CBLBlobReader_Close(actual)
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
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
}

@OptIn(ExperimentalStdlibApi::class)
private fun Source.blobWriteStream(db: Database): CPointer<CBLBlobWriteStream> {
    val writer = wrapCBLError { error ->
        CBLBlobWriter_Create(db.actual, error)
    }!!
    try {
        val bufferSize = 8 * 1024
        val buffer = ByteArray(bufferSize)
        use { source ->
            while (!source.exhausted()) {
                val read = source.readAtMostTo(buffer)
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
    val fs = SystemFileSystem
    if (!fs.exists(path)) {
        throw FileNotFoundException("$this: open failed: ENOENT (No such file or directory)")
    }
    return fs.source(path).buffered()
}

@OptIn(ExperimentalNativeApi::class)
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
    return Path(this)
}

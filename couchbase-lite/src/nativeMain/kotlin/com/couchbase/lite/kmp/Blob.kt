package com.couchbase.lite.kmp

import cnames.structs.CBLBlob
import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.toByteArray
import com.couchbase.lite.kmp.internal.fleece.toFLSlice
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.wrapCBLError
import com.soywiz.korio.file.std.cwdVfs
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import libcblite.*
import okio.*
import kotlin.native.internal.createCleaner

private const val MIME_UNKNOWN = "application/octet-stream"

public actual class Blob
internal constructor(internal val actual: CPointer<CBLBlob>) {

    init {
        CBLBlob_Retain(actual)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLBlob_Release(it)
    }

    public actual constructor(contentType: String, content: ByteArray) :
            this(contentType.toFLString(), content.toFLSlice())

    internal constructor(content: ByteArray) : this(MIME_UNKNOWN, content)

    internal constructor(
        contentType: CValue<FLString> = MIME_UNKNOWN.toFLString(),
        content: CValue<FLSlice>
    ) : this(CBLBlob_CreateWithData(contentType, content)!!) {
        CBLBlob_Release(actual)
    }

    // TODO: stream data
    public actual constructor(contentType: String, stream: Source) :
            this(contentType, stream.buffer().readByteArray())

    // TODO: stream data
    @Throws(IOException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        runBlocking {
            val file = cwdVfs[fileURL]
            if (!file.exists()) {
                throw FileNotFoundException("$fileURL: open failed: ENOENT (No such file or directory)")
            }
            file.read()
        }
    )

    public actual val content: ByteArray?
        get() {
            return wrapCBLError { error ->
                CBLBlob_Content(actual, error).toByteArray()
            }
            // TODO: throw exception when database is closed
        }

    public actual val contentStream: Source?
        get() = wrapCBLError { error ->
            // TODO: stream data
            //CBLBlob_OpenContentStream(actual, error)?.source()
            null
        }

    public actual val contentType: String
        get() = CBLBlob_ContentType(actual).toKString() ?: MIME_UNKNOWN

    public actual fun toJSON(): String {
        if (digest == null) {
            throw IllegalStateException("A Blob may be encoded as JSON only after it has been saved in a database")
        }
        return FLValue_ToJSON(CBLBlob_Properties(actual)?.reinterpret()).toKString()!!
    }

    public actual val length: Long
        get() = CBLBlob_Length(actual).toLong()

    public actual val digest: String?
        get() = CBLBlob_Digest(actual).toKString()

    public actual val properties: Map<String, Any?>
        get() = CBLBlob_Properties(actual)!!.toMap()

    public actual companion object {

        public actual fun isBlob(props: Map<String, Any?>?): Boolean {
            props ?: return false
            return FLDict_IsBlob(MutableDictionary(props).actual)
        }
    }
}

internal fun CPointer<CBLBlob>.asBlob() = Blob(this)

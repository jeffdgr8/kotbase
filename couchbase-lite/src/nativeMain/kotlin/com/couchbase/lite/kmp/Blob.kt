package com.couchbase.lite.kmp

import cnames.structs.CBLBlob
import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.wrapCBLError
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

    @Throws(IOException::class)
    public actual constructor(contentType: String, fileURL: String) : this(
        contentType,
        @Suppress("RedundantLambdaOrAnonymousFunction") {
            if (access(fileURL, R_OK) == -1 && errno == EINVAL) {
                throw IllegalArgumentException("$fileURL must be a file-based URL.")
            }
            val path = fileURL.toPath()
            val fs = FileSystem.SYSTEM
            if (!fs.exists(path)) {
                throw FileNotFoundException("$fileURL: open failed: ENOENT (No such file or directory)")
            }
            fs.source(path)
        }()
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

    private val db: CPointer<CBLDatabase>?
        get() {
            // TODO: have Couchbase add private C API to access
            //  or change digest behavior to be null until installed in database like Java/ObjC
            // hack to access private _db, protected database() in CBLBlob C++ class
            return actual.reinterpret<LongVar>()[7].toCPointer()
        }

    public actual val digest: String?
        get() {
            // Java SDK sets digest only after installed in database
            return if (db == null) null
            else CBLBlob_Digest(actual).toKString()
        }

    public actual val properties: Map<String, Any?>
        get() = CBLBlob_Properties(actual)!!.toMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Blob) return false
        return if (digest != null && other.digest != null) {
            digest == other.digest
        } else {
            content.contentEquals(other.content)
        }
    }

    override fun hashCode(): Int =
        content.contentHashCode()

    override fun toString(): String =
        "Blob{${super.toString()}: $digest($contentType, $length)}"

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

internal fun CPointer<CBLBlob>.asBlob() = Blob(this)

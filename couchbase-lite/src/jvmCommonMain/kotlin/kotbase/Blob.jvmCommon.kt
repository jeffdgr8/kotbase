package kotbase

import kotbase.base.DelegatedClass
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import com.couchbase.lite.Blob as CBLBlob

public actual class Blob
internal constructor(actual: CBLBlob) : DelegatedClass<CBLBlob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(CBLBlob(contentType, content))

    public actual constructor(contentType: String, stream: Source) : this(
        CBLBlob(contentType, stream.asInputStream())
    )

    @Throws(IOException::class)
    public actual constructor(contentType: String, fileURL: String) : this(CBLBlob(contentType, fileURL.toFileUrl()))

    public actual val content: ByteArray?
        get() = actual.content

    public actual val contentStream: Source?
        get() = actual.contentStream?.asSource()?.buffered()

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
            CBLBlob.isBlob(props)
    }
}

internal fun CBLBlob.asBlob() = Blob(this)

private fun String.toFileUrl(): URL {
    return try {
        URL(this)
    } catch (e: MalformedURLException) {
        File(this).toURI().toURL()
    }
}

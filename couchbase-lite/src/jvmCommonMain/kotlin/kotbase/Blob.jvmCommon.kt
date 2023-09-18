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

import kotbase.internal.DelegatedClass
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

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

import cocoapods.CouchbaseLite.CBLBlob
import cocoapods.CouchbaseLite.CBLDatabase
import kotbase.internal.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toNSData
import kotbase.ext.wrapCBLError
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.asNSInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.valueForKey

public actual class Blob
internal constructor(actual: CBLBlob) : DelegatedClass<CBLBlob>(actual) {

    public actual constructor(contentType: String, content: ByteArray) : this(
        CBLBlob(contentType, content.toNSData())
    )

    public actual constructor(contentType: String, stream: Source) : this(
        CBLBlob(contentType, stream.asNSInputStream())
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
        get() = actual.contentStream?.asSource()?.buffered()

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

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

import kotlinx.io.IOException
import kotlinx.io.Source

/**
 * A Couchbase Lite Blob.
 * A Blob appears as a property of a Document and contains arbitrary binary data, tagged with MIME type.
 * Blobs can be arbitrarily large, although some operations may require that the entire content be loaded into memory.
 * The containing document's JSON contains only the Blob's metadata (type, length and digest).  The data itself
 * is stored in a file whose name is the content digest (like git).
 **/
public expect class Blob

/**
 * Construct a Blob with the given in-memory data.
 *
 * @param contentType The type of content this Blob will represent
 * @param content     The data that this Blob will contain
 */
constructor(contentType: String, content: ByteArray) {

    /**
     * Construct a Blob with the given stream of data.
     * The passed stream will be closed when it is copied either to memory
     * (see `getContent`) or to the database.
     * If it is closed before that, by client code, the attempt to store the blob will fail.
     * The converse is also true: the stream for a blob that is not saved or copied to memory
     * will not be closed (except during garbage collection).
     *
     * @param contentType The type of content this Blob will represent
     * @param stream      The stream of data that this Blob will consume
     */
    public constructor(contentType: String, stream: Source)

    /**
     * Construct a Blob with the content of a file.
     * The blob can then be added as a property of a Document.
     * This constructor creates a stream that is not closed until the blob is stored in the db,
     * or copied to memory (except by garbage collection).
     *
     * @param contentType The type of content this Blob will represent
     * @param fileURL     A URL to a file containing the data that this Blob will represent.
     * @throws IOException on failure to open the file URL
     */
    @Throws(IOException::class)
    public constructor(contentType: String, fileURL: String)

    /**
     * Gets the contents of this blob as an in-memory byte array.
     * **Using this method will cause the entire contents of the blob to be read into memory!**
     */
    public val content: ByteArray?

    /**
     * Get the contents of this blob as a stream.
     * The caller is responsible for closing the stream returned by this call.
     * Closing or deleting the database before this call completes may cause it to fail.
     * **When called on a blob created from a stream (or a file path), this method will return null!**
     */
    public val contentStream: Source?

    /**
     * The type of the content this blob contains. By convention this is a MIME type.
     */
    public val contentType: String

    public fun toJSON(): String

    /**
     * The number of bytes of content this blob contains
     * or 0 if initialized with a stream.
     */
    public val length: Long

    /**
     * The cryptographic digest of this Blob's contents, which uniquely identifies it
     * or null if the content has not been saved in a database
     */
    public val digest: String?

    /**
     * The blob metadata
     */
    public val properties: Map<String, Any?>

    public companion object {

        public fun isBlob(props: Map<String, Any?>?): Boolean
    }
}

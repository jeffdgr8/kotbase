package com.couchbase.lite.kmm

import com.couchbase.lite.getBlob
import com.couchbase.lite.kmm.internal.utils.FileUtils
import com.couchbase.lite.kmm.internal.utils.PlatformUtils
import com.couchbase.lite.kmm.internal.utils.StringUtils
import com.couchbase.lite.kmm.internal.utils.TestUtils.assertThrows
import com.couchbase.lite.saveBlob
import com.udobny.kmm.test.IgnoreIos
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okio.*
import kotlin.test.*


// There are other blob tests in test suites...
class BlobTest : BaseDbTest() {

    private lateinit var localBlobContent: String

    @BeforeTest
    fun setUpBlobTest() {
        localBlobContent = StringUtils.randomString(100)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testEquals() {
        val content1a = BLOB_CONTENT.encodeToByteArray()
        val content1b = BLOB_CONTENT.encodeToByteArray()
        val content2a = localBlobContent.encodeToByteArray()

        // store blob
        val data1a = Blob("text/plain", content1a)
        val data1b = Blob("text/plain", content1b)
        val data1c = Blob("text/plain", content1a) // not store in db
        val data2a = Blob("text/plain", content2a)

        assertEquals(data1a, data1b)
        assertEquals(data1b, data1a)
        assertNotEquals(data1a, data2a)
        assertNotEquals(data1b, data2a)
        assertNotEquals(data2a, data1a)
        assertNotEquals(data2a, data1b)

        val mDoc = MutableDocument()
        mDoc.setBlob("blob1a", data1a)
        mDoc.setBlob("blob1b", data1b)
        mDoc.setBlob("blob2a", data2a)
        val doc = saveDocInBaseTestDb(mDoc)

        val blob1a = doc.getBlob("blob1a")
        val blob1b = doc.getBlob("blob1b")
        val blob2a = doc.getBlob("blob2a")

        assertEquals(blob1a, blob1b)
        assertEquals(blob1b, blob1a)
        assertNotEquals(blob1a, blob2a)
        assertNotEquals(blob1b, blob2a)
        assertNotEquals(blob2a, blob1a)
        assertNotEquals(blob2a, blob1b)

        assertEquals(blob1a, data1c)
        assertEquals(data1c, blob1a)
    }

    @Test
    @Throws(CouchbaseLiteException::class)
    fun testHashCode() {
        val content1a = BLOB_CONTENT.encodeToByteArray()
        val content1b = BLOB_CONTENT.encodeToByteArray()
        val content2a = localBlobContent.encodeToByteArray()

        // store blob
        val data1a = Blob("text/plain", content1a)
        val data1b = Blob("text/plain", content1b)
        val data1c = Blob("text/plain", content1a) // not store in db
        val data2a = Blob("text/plain", content2a)

        assertEquals(data1a.hashCode(), data1b.hashCode())
        assertEquals(data1b.hashCode(), data1a.hashCode())
        assertNotEquals(data1a.hashCode(), data2a.hashCode())
        assertNotEquals(data1b.hashCode(), data2a.hashCode())
        assertNotEquals(data2a.hashCode(), data1a.hashCode())
        assertNotEquals(data2a.hashCode(), data1b.hashCode())

        val mDoc = MutableDocument()
        mDoc.setBlob("blob1a", data1a)
        mDoc.setBlob("blob1b", data1b)
        mDoc.setBlob("blob2a", data2a)
        val doc = saveDocInBaseTestDb(mDoc)

        val blob1a = doc.getBlob("blob1a")
        val blob1b = doc.getBlob("blob1b")
        val blob2a = doc.getBlob("blob2a")

        assertEquals(blob1a.hashCode(), blob1b.hashCode())
        assertEquals(blob1b.hashCode(), blob1a.hashCode())
        assertNotEquals(blob1a.hashCode(), blob2a.hashCode())
        assertNotEquals(blob1b.hashCode(), blob2a.hashCode())
        assertNotEquals(blob2a.hashCode(), blob1a.hashCode())
        assertNotEquals(blob2a.hashCode(), blob1b.hashCode())

        assertEquals(blob1a.hashCode(), data1c.hashCode())
        assertEquals(data1c.hashCode(), blob1a.hashCode())
    }

    @Test
    @Throws(IOException::class, CouchbaseLiteException::class)
    fun testBlobContentBytes() {
        val blobContent = PlatformUtils.getAsset("attachment.png")!!.use { input ->
            input.buffer().readByteArray()
        }

        val blob = Blob("image/png", blobContent)
        val mDoc = MutableDocument("doc1")
        mDoc.setBlob("blob", blob)
        val doc = saveDocInBaseTestDb(mDoc)

        val savedBlob = doc.getBlob("blob")
        assertNotNull(savedBlob)

        val buff = blob.content
        assertEquals(blobContent.size, buff!!.size)
        assertContentEquals(blobContent, buff)

        assertEquals(blobContent.size.toLong(), savedBlob.length)

        assertEquals("image/png", savedBlob.contentType)
    }

    // TODO: https://github.com/square/okio/pull/1123
    @Test
    @Throws(CouchbaseLiteException::class, IOException::class)
    fun testBlobContentStream() {
        PlatformUtils.getAsset("attachment.png")!!.use { input ->
            val blob = Blob("image/png", input)
            val mDoc = MutableDocument("doc1")
            mDoc.setBlob("blob", blob)
            baseTestDb.save(mDoc)
        }

        val doc = baseTestDb.getDocument("doc1")
        val savedBlob = doc!!.getBlob("blob")
        assertNotNull(savedBlob)

        val blobContent = PlatformUtils.getAsset("attachment.png")!!.use { input ->
            input.buffer().readByteArray()
        }

        val buff = savedBlob.contentStream!!.use { input ->
            input.buffer().readByteArray()
        }

        assertEquals(blobContent.size, buff.size)
        assertContentEquals(blobContent, buff)

        assertEquals(blobContent.size.toLong(), savedBlob.length)

        assertEquals("image/png", savedBlob.contentType)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1438
    @Test
    @Throws(IOException::class, CouchbaseLiteException::class)
    fun testGetContent6MBFile() {
        val bytes = PlatformUtils.getAsset("iTunesMusicLibrary.json")!!.use { input ->
            input.buffer().readByteArray()
        }

        val blob = Blob("application/json", bytes)
        val mDoc = MutableDocument("doc1")
        mDoc.setBlob("blob", blob)
        val doc = saveDocInBaseTestDb(mDoc)
        val savedBlob = doc.getBlob("blob")
        assertNotNull(savedBlob)
        assertEquals("application/json", savedBlob.contentType)
        val content = blob.content
        assertContentEquals(content, bytes)
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1611
    @Test
    @Throws(IOException::class, CouchbaseLiteException::class)
    fun testGetNonCachedContent6MBFile() {
        val bytes = PlatformUtils.getAsset("iTunesMusicLibrary.json")!!.use { input ->
            input.buffer().readByteArray()
        }

        val blob = Blob("application/json", bytes)
        val mDoc = MutableDocument("doc1")
        mDoc.setBlob("blob", blob)
        val doc = saveDocInBaseTestDb(mDoc)

        // Reload the doc from the database to make sure to "bust the cache" for the blob
        // cached in the doc object
        val reloadedDoc = baseTestDb.getDocument(doc.id)
        val savedBlob = reloadedDoc!!.getBlob("blob")
        val content = savedBlob!!.content
        assertContentEquals(content, bytes)
    }

    @Test
    @Throws(IOException::class)
    fun testBlobFromFileURL() {
        try {
            val contentType = "image/png"
            val path = "$tmpDir/attachment.png"

            val blob = PlatformUtils.getAsset("attachment.png")!!.use { input ->
                val bytes = input.buffer().readByteArray()
                FileUtils.write(bytes, path)
                Blob(contentType, path)
            }

            val bytes = FileUtils.read(path)
            val content = blob.content
            assertContentEquals(content, bytes)

            assertThrows(IllegalArgumentException::class) {
                Blob(contentType, "http://java.sun.com")
            }
        } finally {
            FileUtils.deleteContents(tmpDir)
        }
    }

    // TODO: https://github.com/square/okio/pull/1123
    @Test
    @Throws(IOException::class)
    fun testBlobReadByte() {
        val data = PlatformUtils.getAsset("iTunesMusicLibrary.json")!!.use { input ->
            input.buffer().readByteArray()
        }

        val buffer = Buffer()
        Blob("application/json", data).contentStream!!.read(buffer, 1)
        assertEquals(data[0], buffer[0])
    }

    // TODO: https://github.com/square/okio/pull/1123
    @Test
    @Throws(IOException::class)
    fun testBlobReadByteArray() {
        val data = PlatformUtils.getAsset("iTunesMusicLibrary.json")!!.use { input ->
            input.buffer().readByteArray()
        }

        val blobContent = Blob("application/json", data).contentStream!!.buffer().readByteArray()
        assertContentEquals(data, blobContent)
    }

    // TODO: https://github.com/square/okio/pull/1123
    @Test
    @Throws(IOException::class)
    fun testBlobReadSkip() {
        val data = PlatformUtils.getAsset("iTunesMusicLibrary.json")!!.use { input ->
            input.buffer().readByteArray()
        }

        val blobStream = Blob("application/json", data).contentStream!!.buffer()
        blobStream.skip(17)
        assertEquals(blobStream.readByte(), data[17])
    }

    // TODO: https://github.com/square/okio/pull/1123
    @Test
    @Throws(IOException::class, CouchbaseLiteException::class)
    fun testReadBlobStream() {
        val bytes = PlatformUtils.getAsset("attachment.png")!!.use { input ->
            input.buffer().readByteArray()
        }

        val blob = Blob("image/png", bytes)
        val mDoc = MutableDocument("doc1")
        mDoc.setBlob("blob", blob)
        val doc = saveDocInBaseTestDb(mDoc)

        val savedBlob = doc.getBlob("blob")
        assertNotNull(savedBlob)
        assertEquals("image/png", savedBlob.contentType)

        savedBlob.contentStream!!.use { input ->
            val readBytes = input.buffer().readByteArray()
            assertContentEquals(bytes, readBytes)
        }
    }

    ///////////////  JSON tests

    // 3.1.a
    @Test
    fun testDbSaveBlob() {
        val blob = makeBlob()
        baseTestDb.saveBlob(blob)
        verifyBlob(Json.parseToJsonElement(blob.toJSON()).jsonObject)
    }

    // TODO: iOS doesn't update its size after DB get
    //  https://forums.couchbase.com/t/objc-sdk-doesnt-set-length-after-database-getblob/34077
    @IgnoreIos
    // 3.1.b
    @Test
    fun testDbGetBlob() {
        val props = getPropsForSavedBlob()

        val fetchProps = mutableMapOf<String, Any?>()
        fetchProps[META_PROP_TYPE] = TYPE_BLOB
        fetchProps[PROP_DIGEST] = props[PROP_DIGEST]
        fetchProps[PROP_CONTENT_TYPE] = props[PROP_CONTENT_TYPE]
        val dbBlob = baseTestDb.getBlob(fetchProps)

        verifyBlob(dbBlob)
        assertEquals(BLOB_CONTENT, dbBlob?.content?.decodeToString())
    }

    // 3.1.c
    @Test
    fun testUnsavedBlobToJSON() {
        assertFailsWith<IllegalStateException> {
            makeBlob().toJSON()
        }
    }

    // 3.1.d
    @Test
    fun testDbGetNonexistentBlob() {
        val props = mutableMapOf<String, Any?>()
        props[META_PROP_TYPE] = TYPE_BLOB
        props[PROP_DIGEST] = "sha1-C+ThisIsTheWayWeMakeItFail="
        assertNull(baseTestDb.getBlob(props))
    }

    // 3.1.e.1: empty param
    @Test
    fun testDbGetNotBlob1() {
        assertFailsWith<IllegalArgumentException> {
            val blob = makeBlob()
            baseTestDb.saveBlob(blob)
            assertNull(baseTestDb.getBlob(emptyMap()))
        }
    }

    // 3.1.e.2: missing digest
    @Test
    fun testDbGetNotBlob2() {
        assertFailsWith<IllegalArgumentException> {
            val props = getPropsForSavedBlob().toMutableMap()
            props.remove(PROP_DIGEST)
            assertNull(baseTestDb.getBlob(props))
        }
    }

    // 3.1.e.3: missing meta-type
    @Test
    fun testDbGetNotBlob3() {
        assertFailsWith<IllegalArgumentException> {
            val props = getPropsForSavedBlob().toMutableMap()
            props.remove(META_PROP_TYPE)
            assertNull(baseTestDb.getBlob(props))
        }
    }

    // 3.1.e.4: length is not a number
    @Test
    fun testDbGetNotBlob4() {
        assertFailsWith<IllegalArgumentException> {
            val props = getPropsForSavedBlob().toMutableMap()
            props[PROP_LENGTH] = "42"
            assertNull(baseTestDb.getBlob(props))
        }
    }

    // 3.1.e.5: bad content type
    @Test
    fun testDbGetNotBlob5() {
        assertFailsWith<IllegalArgumentException> {
            val props = getPropsForSavedBlob().toMutableMap()
            props[PROP_CONTENT_TYPE] = Any()
            assertNull(baseTestDb.getBlob(props))
        }
    }

    // 3.1.e.6: extra arg
    @Test
    fun testDbGetNotBlob6() {
        assertFailsWith<IllegalArgumentException> {
            val props = getPropsForSavedBlob().toMutableMap()
            props["foo"] = "bar"
            assertNull(baseTestDb.getBlob(props))
        }
    }

    // 3.1.f
    @Test
    fun testBlobInDocument() {
        val mDoc = MutableDocument()
        mDoc.setBlob("blob", makeBlob())

        val dbBlob = saveDocInBaseTestDb(mDoc).getBlob("blob")

        verifyBlob(dbBlob)

        verifyBlob(Json.parseToJsonElement(dbBlob!!.toJSON()).jsonObject)
    }

    // 3.1.h
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testBlobGoneAfterCompact() {
        val blob = makeBlob()
        baseTestDb.saveBlob(blob)

        assertTrue(baseTestDb.performMaintenance(MaintenanceType.COMPACT))

        val props = mutableMapOf<String, Any?>()
        props[META_PROP_TYPE] = TYPE_BLOB
        props[PROP_DIGEST] = blob.digest

        assertNull(baseTestDb.getBlob(props))
    }

    @Test
    @Throws(IOException::class, CouchbaseLiteException::class)
    fun testIsBlob() {
        PlatformUtils.getAsset("attachment.png")!!.use { input ->
            val blob = Blob("image/png", input)
            val mDoc = MutableDocument("doc1")
            mDoc.setBlob("blob", blob)
            baseTestDb.save(mDoc)
        }

        assertTrue(
            Blob.isBlob(
                MutableDictionary().setJSON(
                    baseTestDb.getDocument("doc1")!!.getBlob("blob")!!.toJSON()
                ).toMap()
            )
        )
    }

    // https://issues.couchbase.com/browse/CBL-2320
    @Test
    @Throws(CouchbaseLiteException::class, IOException::class)
    fun testBlobStreamReadNotNegative() {
        val mDoc = MutableDocument("blobDoc")
        mDoc.setBlob(
            "blob",
            Blob(
                "application/octet-stream",
                byteArrayOf(-1, 255.toByte(), 0xf0.toByte(), 0xa0.toByte())
            )
        )
        saveDocInBaseTestDb(mDoc)

        val blobStream = baseTestDb.getDocument("blobDoc")!!
            .getBlob("blob")!!.contentStream!!.buffer()

        assertEquals(255.toByte(), blobStream.readByte())
        assertEquals(255.toByte(), blobStream.readByte())
        assertEquals(0xf0.toByte(), blobStream.readByte())
        assertEquals(0xa0.toByte(), blobStream.readByte())
    }

    private fun getPropsForSavedBlob(): Map<String, Any?> {
        val blob = makeBlob()
        baseTestDb.saveBlob(blob)
        return blob.properties
    }

    companion object {

        /**
         * The sub-document property that identifies it as a special type of object.
         * For example, a blob is represented as `{"@type":"blob", "digest":"xxxx", ...}`
         */
        const val META_PROP_TYPE = "@type"
        const val TYPE_BLOB = "blob"

        const val PROP_DIGEST = "digest"
        const val PROP_LENGTH = "length"
        const val PROP_CONTENT_TYPE = "content_type"
    }
}

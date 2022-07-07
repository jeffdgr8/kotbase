//// TODO:
//package com.couchbase.lite.kmm
//
//import com.couchbase.lite.kmm.internal.utils.PlatformUtils
//import com.couchbase.lite.kmm.internal.utils.StringUtils
//import okio.use
//import org.junit.rules.TemporaryFolder
//import sun.misc.IOUtils
//import kotlin.test.*
//
//
//// There are other blob tests in test suites...
//class BlobTest : BaseDbTest {
//
//    private lateinit var localBlobContent: String
//
//    @Rule
//    var tempFolder: TemporaryFolder = TemporaryFolder()
//
//    @BeforeTest
//    fun setUpBlobTest() {
//        localBlobContent = StringUtils.randomString(100)
//    }
//
//    @Test
//    @Throws(CouchbaseLiteException::class)
//    fun testEquals() {
//        val content1a: ByteArray = BLOB_CONTENT.encodeToByteArray()
//        val content1b: ByteArray = BLOB_CONTENT.encodeToByteArray()
//        val content2a: ByteArray = localBlobContent.encodeToByteArray()
//
//        // store blob
//        val data1a = Blob("text/plain", content1a)
//        val data1b = Blob("text/plain", content1b)
//        val data1c = Blob("text/plain", content1a) // not store in db
//        val data2a = Blob("text/plain", content2a)
//        assertEquals(data1a, data1b)
//        assertEquals(data1b, data1a)
//        assertNotEquals(data1a, data2a)
//        assertNotEquals(data1b, data2a)
//        assertNotEquals(data2a, data1a)
//        assertNotEquals(data2a, data1b)
//        val mDoc = MutableDocument()
//        mDoc.setBlob("blob1a", data1a)
//        mDoc.setBlob("blob1b", data1b)
//        mDoc.setBlob("blob2a", data2a)
//        val doc = saveDocInBaseTestDb(mDoc)
//        val blob1a = doc.getBlob("blob1a")
//        val blob1b = doc.getBlob("blob1b")
//        val blob2a = doc.getBlob("blob2a")
//        assertEquals(blob1a, blob1b)
//        assertEquals(blob1b, blob1a)
//        assertNotEquals(blob1a, blob2a)
//        assertNotEquals(blob1b, blob2a)
//        assertNotEquals(blob2a, blob1a)
//        assertNotEquals(blob2a, blob1b)
//        assertEquals(blob1a, data1c)
//        assertEquals(data1c, blob1a)
//    }
//
//    @Test
//    @Throws(CouchbaseLiteException::class)
//    fun testHashCode() {
//        val content1a: ByteArray = BLOB_CONTENT.encodeToByteArray()
//        val content1b: ByteArray = BLOB_CONTENT.encodeToByteArray()
//        val content2a: ByteArray = localBlobContent.encodeToByteArray()
//
//        // store blob
//        val data1a = Blob("text/plain", content1a)
//        val data1b = Blob("text/plain", content1b)
//        val data1c = Blob("text/plain", content1a) // not store in db
//        val data2a = Blob("text/plain", content2a)
//        assertEquals(data1a.hashCode(), data1b.hashCode())
//        assertEquals(data1b.hashCode(), data1a.hashCode())
//        assertNotEquals(data1a.hashCode(), data2a.hashCode())
//        assertNotEquals(data1b.hashCode(), data2a.hashCode())
//        assertNotEquals(data2a.hashCode(), data1a.hashCode())
//        assertNotEquals(data2a.hashCode(), data1b.hashCode())
//        val mDoc = MutableDocument()
//        mDoc.setBlob("blob1a", data1a)
//        mDoc.setBlob("blob1b", data1b)
//        mDoc.setBlob("blob2a", data2a)
//        val doc = saveDocInBaseTestDb(mDoc)
//        val blob1a = doc.getBlob("blob1a")
//        val blob1b = doc.getBlob("blob1b")
//        val blob2a = doc.getBlob("blob2a")
//        assertEquals(blob1a.hashCode(), blob1b.hashCode())
//        assertEquals(blob1b.hashCode(), blob1a.hashCode())
//        assertNotEquals(blob1a.hashCode(), blob2a.hashCode())
//        assertNotEquals(blob1b.hashCode(), blob2a.hashCode())
//        assertNotEquals(blob2a.hashCode(), blob1a.hashCode())
//        assertNotEquals(blob2a.hashCode(), blob1b.hashCode())
//        assertEquals(blob1a.hashCode(), data1c.hashCode())
//        assertEquals(data1c.hashCode(), blob1a.hashCode())
//    }
//
//    @Test
//    @Throws(IOException::class, CouchbaseLiteException::class)
//    fun testBlobContentBytes() {
//        val blobContent: ByteArray = PlatformUtils.getAsset("attachment.png")
//            .use { input -> IOUtils.toByteArray(input) }
//        val blob = Blob("image/png", blobContent)
//        val mDoc = MutableDocument("doc1")
//        mDoc.setBlob("blob", blob)
//        val doc = saveDocInBaseTestDb(mDoc)
//        val savedBlob = doc.getBlob("blob")
//        assertNotNull(savedBlob)
//        val buff = blob.content
//        assertEquals(blobContent.size, buff!!.size)
//        assertContentEquals(blobContent, buff)
//        assertEquals(blobContent.size.toLong(), savedBlob.length)
//        assertEquals("image/png", savedBlob.contentType)
//    }
//}

package kotbase

import kotlin.test.*

class URLEndpointTest : BaseTest() {

    // TODO: Java updated to behave like ObjC in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/4
    @Ignore
    @Test
    fun testEmbeddedUserForbidden() {
        assertFailsWith<IllegalArgumentException> {
            URLEndpoint("ws://user@couchbase.com/sg")
        }
    }

    @Test
    fun testEmbeddedPasswordNotAllowed() {
        assertFailsWith<IllegalArgumentException> {
            URLEndpoint("ws://user:pass@couchbase.com/sg")
        }
    }

    @Test
    fun testBadScheme() {
        val uri = "http://4.4.4.4:4444"
        var err: Exception? = null
        try {
            URLEndpoint(uri)
        } catch (e: Exception) {
            err = e
        }
        assertNotNull(err)
        assertTrue(err.message!!.contains(uri))
    }
}

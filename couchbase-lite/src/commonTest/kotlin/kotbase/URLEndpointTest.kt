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

import kotlin.test.*

class URLEndpointTest : BaseTest() {

    @Test
    fun testEmbeddedUserForbidden() {
        assertFailsWith<IllegalArgumentException> { URLEndpoint("ws://user@couchbase.com/sg") }
    }

    @Test
    fun testEmbeddedPasswordNotAllowed() {
        assertFailsWith<IllegalArgumentException> { URLEndpoint("ws://user:pass@couchbase.com/sg") }
    }

    @Test
    fun testBadScheme() {
        val uri = "http://4.4.4.4:4444"
        var err: Exception? = null
        try { URLEndpoint(uri) }
        catch (e: Exception) { err = e }
        assertNotNull(err)
        assertTrue(err.message!!.contains(uri))
    }
}

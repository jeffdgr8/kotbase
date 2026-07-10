/*
 * Copyright 2026 Jeff Lockhart
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

import cocoapods.CouchbaseLite.CBLErrorHTTPNotFound
import kotlin.test.*

// URLEndpointListenerTest+Collection.m

// TODO: many tests fail with
//  CouchbaseLiteException{CouchbaseLite,22,'Couldn't add a certificate to the Keychain (SecItemAdd returned -25299)'}
//  or
//  kotlin.NullPointerException
//  https://youtrack.jetbrains.com/issue/KT-61470#focus=Comments-27-8789456.0-0 may be helpful
// TODO: identities are not cleaned up in keychain in URLEndpointListenerBaseTest (should be in finally {} block)
@Ignore
class URLEndpointListenerCollectionTest : URLEndpointListenerBaseTest() {

    private fun createDocNumbered(collection: Collection, start: Int, num: Int) {
        for (i in start until start + num) {
            val doc = MutableDocument("doc-$i")
            doc.setInt("number1", i)
            collection.save(doc)
        }
    }

    @Test
    fun testCollectionsSingleShotPushPullReplication() {
        if (!keyChainAccessAllowed) return

        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col2a, 0, 3)
        createDocNumbered(col2b, 10, 5)
        assertEquals(0, col1a.count)
        assertEquals(0, col1b.count)
        assertEquals(3, col2a.count)
        assertEquals(5, col2b.count)

        val config = URLEndpointListenerConfiguration(setOf(col2a, col2b))
        startListener(config)

        val rConfig = ReplicatorConfiguration(
            CollectionConfiguration.fromCollections(listOf(col1a, col1b)),
            listener!!.localURLEndpoint
        )
        rConfig.pinnedServerCertificate = listener!!.tlsIdentity?.certs?.get(0)

        rConfig.run()

        assertEquals(3, col1a.count)
        assertEquals(5, col1b.count)
        assertEquals(3, col2a.count)
        assertEquals(5, col2b.count)

        stopListener()
    }

    @Test
    fun testCollectionsContinuousPushPullReplication() {
        if (!keyChainAccessAllowed) return

        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col2a, 0, 10)
        createDocNumbered(col2b, 10, 10)

        val config = URLEndpointListenerConfiguration(setOf(col2a, col2b))
        startListener(config)

        val rConfig = ReplicatorConfiguration(
            CollectionConfiguration.fromCollections(listOf(col1a, col1b)),
            listener!!.localURLEndpoint
        )
        rConfig.isContinuous = true
        rConfig.pinnedServerCertificate = listener!!.tlsIdentity?.certs?.get(0)

        rConfig.run()

        assertEquals(10, col1a.count)
        assertEquals(10, col1b.count)
        assertEquals(10, col2a.count)
        assertEquals(10, col2b.count)

        stopListener()
    }

    @Test
    fun testMismatchedCollectionReplication() {
        if (!keyChainAccessAllowed) return

        val colA = testDatabase.createCollection("colA", "scopeA")
        createDocNumbered(colA, 0, 10)

        val colB = targetDatabase.createCollection("colB", "scopeA")

        val config = URLEndpointListenerConfiguration(setOf(colB))
        startListener(config)

        val rConfig = ReplicatorConfiguration(
            CollectionConfiguration.fromCollections(listOf(colA)),
            listener!!.localURLEndpoint
        )
        rConfig.pinnedServerCertificate = listener!!.tlsIdentity?.certs?.get(0)

        rConfig.run(code = CBLErrorHTTPNotFound.toInt())

        stopListener()
    }

    // TODO: the underlying CBLURLEndpointListenerConfiguration throws an
    //  NSInvalidArgumentException, which cannot be caught in Kotlin/Native
    @Ignore
    @Test
    fun testCreateListenerConfigWithEmptyCollection() {
        assertFailsWith<IllegalArgumentException> {
            URLEndpointListenerConfiguration(emptySet())
        }
    }
}

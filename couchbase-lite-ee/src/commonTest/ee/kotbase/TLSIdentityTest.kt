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

import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

// TLSIdentityTest.m

class TLSIdentityTest : BaseTest() {

    private val serverCertAttrs = mapOf(TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to "CBL-Server")
    private val clientCertAttrs = mapOf(TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to "CBL-Client")

    @BeforeTest
    fun setUpTLSIdentityTest() {
        if (!keyStoreAccessAllowed) return
        initPlatformKeyStore()
        deleteTestIdentities()
    }

    @AfterTest
    fun tearDownTLSIdentityTest() {
        if (!keyStoreAccessAllowed) return
        deleteTestIdentities()
    }

    private fun deleteTestIdentities() {
        TLSIdentity.deleteIdentity(SERVER_CERT_LABEL)
        TLSIdentity.deleteIdentity(CLIENT_CERT_LABEL)
    }

    @Test
    fun testCreateGetDeleteServerIdentity() {
        if (!keyStoreAccessAllowed) return

        // Get:
        assertNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))

        // Create:
        var identity = TLSIdentity.createIdentity(
            setOf(KeyUsage.SERVER_AUTH),
            serverCertAttrs,
            null,
            SERVER_CERT_LABEL
        )
        assertEquals(1, identity.certs.size)

        // Get:
        identity = assertNonNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))
        assertEquals(1, identity.certs.size)

        // Delete:
        TLSIdentity.deleteIdentity(SERVER_CERT_LABEL)

        // Get:
        assertNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))
    }

    @Test
    fun testCreateDuplicateServerIdentity() {
        if (!keyStoreAccessAllowed) return

        // Create:
        TLSIdentity.createIdentity(
            setOf(KeyUsage.SERVER_AUTH),
            serverCertAttrs,
            null,
            SERVER_CERT_LABEL
        )

        // Get:
        assertNotNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))

        // Create again with the same alias:
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.CRYPTO) {
            TLSIdentity.createIdentity(
                setOf(KeyUsage.SERVER_AUTH),
                serverCertAttrs,
                null,
                SERVER_CERT_LABEL
            )
        }
    }

    @Test
    fun testCreateGetDeleteClientIdentity() {
        if (!keyStoreAccessAllowed) return

        // Get:
        assertNull(TLSIdentity.getIdentity(CLIENT_CERT_LABEL))

        // Create:
        var identity = TLSIdentity.createIdentity(
            setOf(KeyUsage.CLIENT_AUTH),
            clientCertAttrs,
            null,
            CLIENT_CERT_LABEL
        )
        assertEquals(1, identity.certs.size)

        // Get:
        identity = assertNonNull(TLSIdentity.getIdentity(CLIENT_CERT_LABEL))
        assertEquals(1, identity.certs.size)

        // Delete:
        TLSIdentity.deleteIdentity(CLIENT_CERT_LABEL)

        // Get:
        assertNull(TLSIdentity.getIdentity(CLIENT_CERT_LABEL))
    }

    @Test
    fun testCreateDuplicateClientIdentity() {
        if (!keyStoreAccessAllowed) return

        // Create:
        TLSIdentity.createIdentity(
            setOf(KeyUsage.CLIENT_AUTH),
            clientCertAttrs,
            null,
            CLIENT_CERT_LABEL
        )

        // Get:
        assertNotNull(TLSIdentity.getIdentity(CLIENT_CERT_LABEL))

        // Create again with the same alias:
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.CRYPTO) {
            TLSIdentity.createIdentity(
                setOf(KeyUsage.CLIENT_AUTH),
                clientCertAttrs,
                null,
                CLIENT_CERT_LABEL
            )
        }
    }

    @Test
    fun testCreateIdentityWithNoAttributes() {
        if (!keyStoreAccessAllowed) return

        // Get:
        assertNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))

        // Create:
        // Apple throws a CouchbaseLiteException (Crypto), the Java SDKs throw an
        // IllegalArgumentException for the missing Common Name attribute
        val e = assertFails {
            TLSIdentity.createIdentity(
                setOf(KeyUsage.SERVER_AUTH),
                emptyMap(),
                null,
                SERVER_CERT_LABEL
            )
        }
        when (e) {
            is CouchbaseLiteException -> {
                assertEquals(CBLError.Domain.CBLITE, e.domain)
                assertEquals(CBLError.Code.CRYPTO, e.code)
            }
            is IllegalArgumentException -> {} // expected on JVM and Android
            else -> throw AssertionError("Unexpected exception", e)
        }
    }

    @Test
    fun testCertificateExpiration() {
        if (!keyStoreAccessAllowed) return

        // Get:
        assertNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))

        // Create:
        val expiration = Clock.System.now() + 300.seconds
        val identity = TLSIdentity.createIdentity(
            setOf(KeyUsage.SERVER_AUTH),
            serverCertAttrs,
            expiration,
            SERVER_CERT_LABEL
        )
        assertEquals(1, identity.certs.size)

        // The actual expiration will be slightly less than the set expiration time
        // (the JVM's LiteCore certificate generation subtracts a full 60-second
        // clock-drift allowance, Apple platforms only a few seconds):
        val diff = expiration - identity.expiration
        assertTrue(
            diff >= 0.seconds && diff <= 70.seconds,
            "expected $expiration, actual ${identity.expiration}"
        )
    }

    companion object {
        // lowercase: JVM PKCS12 KeyStore aliases are case-insensitive,
        // stored lowercased, and deleted by exact match
        const val SERVER_CERT_LABEL = "cbl-server-cert"
        const val CLIENT_CERT_LABEL = "cbl-client-cert"
    }
}

// On the JVM the KeyStore to use must be registered with TLSIdentity.useKeyStore()
internal expect fun initPlatformKeyStore()

// Keychain access requires a host app on iOS
internal expect val keyStoreAccessAllowed: Boolean

// TODO: TLSIdentity.certs throws ClassCastException on Apple platforms:
//  the SecCertificateRef NSArray elements cannot be cast to CPointer
//  https://youtrack.jetbrains.com/issue/KT-61470
//  workaround counts the certs without accessing the ByteArray elements

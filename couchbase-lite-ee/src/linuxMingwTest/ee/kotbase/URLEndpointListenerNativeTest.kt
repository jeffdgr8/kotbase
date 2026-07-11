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

import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Smoke tests for the CBL C SDK URLEndpointListener and TLSIdentity
 * implementations on Linux and Windows.
 */
@OptIn(ExperimentalNativeApi::class)
class URLEndpointListenerNativeTest : BaseDbTest() {

    private val serverCertAttrs = mapOf(TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to "CBL-Server")
    private val clientCertAttrs = mapOf(TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to "CBL-Client")

    // Anonymous, non-persisted identities are supported on all CBL C platforms
    private fun createAnonymousIdentity(
        usage: KeyUsage = KeyUsage.SERVER_AUTH,
        attributes: Map<String, String> = serverCertAttrs
    ): TLSIdentity = TLSIdentity.createAnonymousIdentity(setOf(usage), attributes)

    // Configuration

    @Test
    fun testConfigDefaults() {
        val config = URLEndpointListenerConfiguration(setOf(testCollection))
        assertEquals(setOf(testCollection), config.collections)
        assertNull(config.networkInterface)
        assertEquals(Defaults.Listener.PORT, config.port)
        assertEquals(Defaults.Listener.DISABLE_TLS, config.isTlsDisabled)
        assertNull(config.tlsIdentity)
        assertNull(config.authenticator)
        assertEquals(Defaults.Listener.READ_ONLY, config.isReadOnly)
        assertEquals(Defaults.Listener.ENABLE_DELTA_SYNC, config.isDeltaSyncEnabled)
    }

    @Test
    fun testConfigPropertyRoundTrip() {
        val identity = createAnonymousIdentity()
        val auth = ListenerPasswordAuthenticator { _, _ -> true }

        val config = URLEndpointListenerConfiguration(setOf(testCollection))
        config.networkInterface = "en0"
        config.port = 12345
        config.isTlsDisabled = true
        config.tlsIdentity = identity
        config.authenticator = auth
        config.isReadOnly = true
        config.isDeltaSyncEnabled = true

        assertEquals("en0", config.networkInterface)
        assertEquals(12345, config.port)
        assertTrue(config.isTlsDisabled)
        assertEquals(identity, config.tlsIdentity)
        assertEquals(auth, config.authenticator)
        assertTrue(config.isReadOnly)
        assertTrue(config.isDeltaSyncEnabled)

        // Copy constructor:
        val copy = URLEndpointListenerConfiguration(config)
        assertEquals(config.collections, copy.collections)
        assertEquals("en0", copy.networkInterface)
        assertEquals(12345, copy.port)
        assertTrue(copy.isTlsDisabled)
        assertEquals(identity, copy.tlsIdentity)
        assertEquals(auth, copy.authenticator)
        assertTrue(copy.isReadOnly)
        assertTrue(copy.isDeltaSyncEnabled)

        // Mutating the copy does not affect the original:
        copy.port = 54321
        assertEquals(12345, config.port)
    }

    @Test
    fun testConfigRequiresCollection() {
        assertFailsWith<IllegalArgumentException> {
            URLEndpointListenerConfiguration(emptySet())
        }
    }

    @Test
    fun testConfigPortRange() {
        val config = URLEndpointListenerConfiguration(setOf(testCollection))
        assertFailsWith<IllegalArgumentException> { config.port = -1 }
        assertFailsWith<IllegalArgumentException> {
            config.port = URLEndpointListenerConfiguration.MAX_PORT + 1
        }
        config.port = URLEndpointListenerConfiguration.MIN_PORT
        config.port = URLEndpointListenerConfiguration.MAX_PORT
    }

    // Listener

    @Test
    fun testListenerBasicLifecycle() {
        val config = URLEndpointListenerConfiguration(
            collections = setOf(testCollection),
            disableTls = true,
            authenticator = ListenerPasswordAuthenticator { username, password ->
                username == "daniel" && password.concatToString() == "123"
            }
        )
        val listener = URLEndpointListener(config)

        // Not started:
        assertNull(listener.port)
        assertTrue(listener.urls.isEmpty())
        assertNull(listener.tlsIdentity)

        listener.start()
        try {
            val port = assertNotNull(listener.port)
            assertTrue(port > 0)

            val urls = listener.urls
            assertTrue(urls.isNotEmpty())
            urls.forEach { url ->
                assertTrue(url.startsWith("ws://"), "unexpected listener URL: $url")
                assertTrue(url.contains(port.toString()), "unexpected listener URL: $url")
            }

            val status = assertNotNull(listener.status)
            assertEquals(0, status.connectionCount)
            assertEquals(0, status.activeConnectionCount)

            // TLS is disabled:
            assertNull(listener.tlsIdentity)

            // Listener config is a snapshot copy:
            val listenerConfig = listener.config
            assertEquals(setOf(testCollection), listenerConfig.collections)
            assertTrue(listenerConfig.isTlsDisabled)
            assertNotNull(listenerConfig.authenticator)
        } finally {
            listener.stop()
        }

        // Stopped:
        assertNull(listener.port)
        assertTrue(listener.urls.isEmpty())
    }

    @Test
    fun testTLSListenerWithAnonymousIdentity() {
        val identity = createAnonymousIdentity()
        assertEquals(1, identity.certs.size)
        assertTrue(identity.certs.first().isNotEmpty())

        val config = URLEndpointListenerConfiguration(
            collections = setOf(testCollection),
            identity = identity
        )
        val listener = URLEndpointListener(config)
        listener.start()
        try {
            assertTrue(assertNotNull(listener.port) > 0)
            listener.urls.forEach { url ->
                assertTrue(url.startsWith("wss://"), "unexpected listener URL: $url")
            }

            val listenerIdentity = assertNotNull(listener.tlsIdentity)
            assertContentEquals(identity.certs.first(), listenerIdentity.certs.first())
        } finally {
            listener.stop()
        }
    }

    @Test
    fun testTLSListenerWithAutoGeneratedIdentity() {
        val config = URLEndpointListenerConfiguration(setOf(testCollection))
        val listener = URLEndpointListener(config)
        listener.start()
        try {
            // With TLS enabled and no identity configured,
            // the listener generates an anonymous identity:
            val identity = assertNotNull(listener.tlsIdentity)
            assertEquals(1, identity.certs.size)
            assertTrue(identity.certs.first().isNotEmpty())
        } finally {
            listener.stop()
        }
    }

    @Test
    fun testTLSListenerWithRootCertAuthenticator() {
        val serverIdentity = createAnonymousIdentity()
        val clientIdentity = createAnonymousIdentity(KeyUsage.CLIENT_AUTH, clientCertAttrs)

        val config = URLEndpointListenerConfiguration(
            collections = setOf(testCollection),
            identity = serverIdentity,
            authenticator = ListenerCertificateAuthenticator(clientIdentity.certs)
        )
        val listener = URLEndpointListener(config)
        listener.start()
        try {
            assertTrue(assertNotNull(listener.port) > 0)
        } finally {
            listener.stop()
        }
    }

    @Test
    fun testTLSListenerWithCertAuthenticatorDelegate() {
        val config = URLEndpointListenerConfiguration(
            collections = setOf(testCollection),
            identity = createAnonymousIdentity(),
            authenticator = ListenerCertificateAuthenticator { certs -> certs.isNotEmpty() }
        )
        val listener = URLEndpointListener(config)
        listener.start()
        try {
            assertTrue(assertNotNull(listener.port) > 0)
        } finally {
            listener.stop()
        }
    }

    // TLSIdentity

    @Test
    fun testAnonymousIdentityCertsAndExpiration() {
        val expiration = Clock.System.now() + 300.seconds
        val identity = TLSIdentity.createAnonymousIdentity(
            setOf(KeyUsage.SERVER_AUTH),
            serverCertAttrs,
            expiration
        )
        assertEquals(1, identity.certs.size)

        // The actual expiration may be slightly less than the
        // requested expiration (clock-drift allowance):
        val diff = expiration - identity.expiration
        assertTrue(
            diff >= 0.seconds && diff <= 70.seconds,
            "expected $expiration, actual ${identity.expiration}"
        )
    }

    @Test
    fun testAnonymousIdentityDefaultExpiration() {
        val identity = createAnonymousIdentity()
        // Default validity is 1 year
        assertTrue(identity.expiration > Clock.System.now() + 300.seconds)
    }

    @Test
    fun testCreateIdentityRequiresCommonName() {
        assertFailsWith<IllegalArgumentException> {
            TLSIdentity.createAnonymousIdentity(setOf(KeyUsage.SERVER_AUTH), emptyMap())
        }
    }

    @Test
    fun testPersistedIdentityNotSupportedOnLinux() {
        if (Platform.osFamily != OsFamily.LINUX) return

        // The CBL C SDK does not support persisting identities
        // with a label on Linux (see CBLTLSIdentity.h):
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.UNSUPPORTED) {
            TLSIdentity.getIdentity("cbl-server-cert")
        }
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.UNSUPPORTED) {
            TLSIdentity.deleteIdentity("cbl-server-cert")
        }
        // Creating an identity with a label propagates the C SDK error:
        assertFailsWith<CouchbaseLiteException> {
            TLSIdentity.createIdentity(
                setOf(KeyUsage.SERVER_AUTH),
                serverCertAttrs,
                null,
                "cbl-server-cert"
            )
        }
    }
}

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

    // TLSIdentity from an existing key pair and certificate chain

    @Test
    fun testCreateIdentityWithKeyPairAndCerts() {
        val identity = TLSIdentity.createIdentityWithKeyPairAndCerts(
            TEST_KEY_PEM.encodeToByteArray(),
            null,
            listOf(TEST_CERT_PEM.encodeToByteArray())
        )
        assertEquals(1, identity.certs.size)
        // the fixture certificate is valid for 10 years from 2026-07-11
        assertTrue(identity.expiration > Clock.System.now())

        // usable as a listener's server identity
        val config = URLEndpointListenerConfiguration(setOf(testCollection), identity = identity)
        val listener = URLEndpointListener(config)
        try {
            listener.start()
            assertEquals(identity.certs, listener.tlsIdentity?.certs)
        } finally {
            listener.stop()
        }
    }

    @Test
    fun testCreateIdentityWithEncryptedKey() {
        val identity = TLSIdentity.createIdentityWithKeyPairAndCerts(
            TEST_KEY_ENCRYPTED_PEM.encodeToByteArray(),
            "kotbase".toCharArray(),
            listOf(TEST_CERT_PEM.encodeToByteArray())
        )
        assertEquals(1, identity.certs.size)
    }

    @Test
    fun testCreateIdentityWithWrongKeyPassword() {
        assertFailsWith<CouchbaseLiteException> {
            TLSIdentity.createIdentityWithKeyPairAndCerts(
                TEST_KEY_ENCRYPTED_PEM.encodeToByteArray(),
                "wrong".toCharArray(),
                listOf(TEST_CERT_PEM.encodeToByteArray())
            )
        }
    }

    @Test
    fun testCreateIdentityWithNoCerts() {
        assertFailsWith<IllegalArgumentException> {
            TLSIdentity.createIdentityWithKeyPairAndCerts(
                TEST_KEY_PEM.encodeToByteArray(),
                null,
                emptyList()
            )
        }
    }

    companion object {

        // Self-signed test fixture (CN=CBL-Test-KeyPair, valid until 2036-07-08),
        // private key also encrypted with the password "kotbase"
        private val TEST_KEY_PEM = """
-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDZC2tlJwOPYP2a
lRVzXnm9slkbvrG2/JKGiw4CEPNyiqmL3oKMNjJto4OvFK7Lqy06CQUGviAR8Vg/
WpA/8OBbk01fITgb+hzjbBj29T3ZtvXAPuXZlDiJCw7ImsryqnM1L/1HZb3DKTe8
JK2cNbEBndn/0ppXsxjU4X+rKT2xoVLrsdWCPQ/DWsdO3hhb5s81xvXpGP+iF35e
6PLEU55HacCOIA3k6K3jhD2Ze2gziEdr2jSJl99wpSHmqleWhtQNW9o6EzHLY5yG
pJ3M2S6jFBUAiZIAijfz2QrNiTDD8CIpuaf2voht5cm21UWRtJ3hv36MnkGqqz5X
B5dXzCYzAgMBAAECggEABy1ouBm4EaRKc/rAkLtWg/P1HNpSu0UofYHYzRMipkPO
Dfhe9QfQAfzUKr89IBX2tpRDVYK/Xk3c9jbtsSMrRmakgZXVv/KmqFqiNnzoGexz
9nLBlRZEfFggfVw3Sv5hGSidqTyoaJfhHy3BjN3gVg118J05u9a0rZ9Jm2gr1qqk
3EQ+ytQjhyfmW73G2ZDzprd7eAohY+UIU2Fqq4ufMsKlteYmOW94OTQU719XbaEP
fbeR8miMyTNsSeF1WZgxukqnNZQvBluMCExyQjMMrhgT9ZfowahtEkedov8840og
8PggHPz4IHKQdUn0ihLc2sDT/KiOfqBtIvGVF+zhiQKBgQD/L0EWJS8JgMBBuuRE
rWDVnJd6HGTPhxlGmfERzIIvXiEASdY37AwYw3piAYGjjpdG7P9yIlwRkHxBRq18
1cIoXuuV2nOYs6C+nJhZG/m1qvidNKdt5owap8XZiVQmBacuO3S4hLTXRX9ooIAM
+gelGPoLLMHZH8mHzkgcpiA57wKBgQDZvPdPPgyGhUr6HdLAAac74OxXWx3HazOV
azmMq/LC4NLjCRlMeQx52dmlouOcV2UGQeHmTV8ZpgSpIvotENTDU9UIbmUbqRB6
/1hx18PLGb+j6JBELWH5VJqLsJgEUMGYaaycoJ8NYE8RSty3SHxCycD3Z8bTgF1q
luqn1gRr/QKBgEZeKXyJ1GayclGtz7SAeutsuviZS9NOVdH6MVfPJTcapc8GjWPu
W4eYn+7Wq4EGmQCd6fL3NpxBU4JMkxqRd3Dae+xeyPGa1wC4/enUpQ9LjjMLif/s
S0YubYiHLgUuGePN0Wnwbk3OhEu9tpHdgK4uLQVX2jydX11rrMb5qHlRAoGAS8sQ
l3/04jqx78DD5Nxud1SDUKFCC3HQofmSLdqJBbc3EZrQOKOGaUAhGBxMeNafLv9u
ADn8mbQFuU6YoWlTdq7O4EF1ZpyCRR5vjilaWW+cGxVQarsVeLIvneKZ+B6YVVKr
qFXtJ73XqCBUFmTgBGuwhS6VsnfjjL/97PB9140CgYEAi244t2m8w1iv4ZaglEsE
JZHYtBhFlDKhJu9U8EDkp7wTc1DOolHsgKfOCwNf1CT6Eg7rOhghFwq7jC/DQgXc
uCy9bUiFiGpVybLrKhyAO8ZbNCoOaHQhVM7edbmHBT2qad2Hdh8KE4C2EyMnjzpD
SE9y0YKK8TyYOiShv4EX6VM=
-----END PRIVATE KEY-----
""".trimIndent()

        private val TEST_KEY_ENCRYPTED_PEM = """
-----BEGIN RSA PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: AES-256-CBC,2F4BA0F92009AC729EEFCC82210A4C17

g4GwUK9AzThKH4lELwV+mNKHYwPkHhdIjZrlYL9l2A8fTUs00P1ORhaPxAdrTm3L
WTOsSnUpEwyvQLYbPV3D+cqJ+ZZDqPyT55JYV8fe9sIrsku3PLRztgF5AiAFwjQN
AJTc9rFKvMMWZJiAAGYseW2Ufdh+zr0vcCbpP6HhuuGbze2SbB0yNisqgJG3zHNe
qZElpFb4SX2YEMlDagIAIcGaCRHD2ggLjMYxTryXvpmFXGGzq0kiZ/jjqRFtpT/P
vQjZjFsvFQPNB5mKiXhsOX+AQG4Ra9qEy86fXovfFgbUHDHyzdoOjvAMQGmoPUht
/6L3BIdv+/qnYRwgBYpLOz7/QruUAvsUQ4vI7Q+OaNitEBPZyIPJVpOQ1t8f6gDv
RPt5/KlUG8iwKKjp7dBxw2SK2UYOHJG2aN1QSAd7wCH8Z6R4M3WmktxKFEKUE2o8
1nH2rfqwM/G8Gt4wHCMFpL/pmrv4IPj5e7CvksqusmZuB04SOCbDw4aMyhQOvDBW
thCDQRC5dMcNDZH4K+f7U47SNXPI/KEo4QAlHwYN964emfu2nGNuUOJ06UWpVno6
C28Yg27LWhWAyWrAHKVKy6PHYVoYabYp4wgkE2kJBZAB3ZeXkiDWfiHEgzR3OACr
8DIsi5ZnevaUmGaYlqbGK/o/ivJlp8j5emYH8xyb3y6S1ZoAPdnehXJNLNkG6xmG
ve89ilRI9prC8VtiUoPW6+OgJPOs4RSbnSF8R/3xVbj42DmTF/refIcxWusNKTEu
86E61F7gP/01AlTZSeATVRIpBQ1uj7K93lRHPM6Q6n0U/J1+jLUsTyUZgvF2RrFG
RMEiPMCBQeHevoiFWQ9Y3ULcKhzxuVjwVdpzHtMKG4K8MV/4UzaksSBagGC2GDEH
djKFi20S1f/CwXbku/m6RxGSrZv1EVcdpx0uHXX+K1j5HH1H7aVUga7y0PPYEIIw
cC/NV/IeGNa5UV6mSi287cGJPnv41rUxecjZESTVNXLpFnhPa5kiT+cRWkGvCJ4p
4LmLyB5+xr7efSPVzW9XwlCC50vDK6rhL0DCM9iri1rYF86Y6DzywSFg+sd4DADF
G/Aj2DLcUuM6O2tc2srIblL3xEkbjqG8MXXyrAN5lIjoAu/jPp9lAM/HtIA8c6zl
avVynCixRynFOSQxDYpUIpLChARjWYxTIwFbVnkJbDcbe33BMVohZNOsLOQ/lthg
dh5cT6XbgLkPveGgRqnvoChkmGO2VPTjl69q3iRWNYoiPPU/Qpsimb5tUt7QoXqt
9M0YJqsh45S1TzHnQVeII/r4FqZraBNVz31EgztSpOE986wyxwU4Kl0Lqib1WWLh
A8MM/IjqjGeqwNRJOWA1/IjuZto3uBXZvoS0Ku2JOuyj1WuezF8xrXbI7dZK9qwn
Sr3aHt6dJT3M2DUBjkauhAneQ04IqLArmISfTAnHo3MBH6zrPqKRf1aJbAt5Ic2J
nRg5eTKD/ggPhzS3F5gg+sfdskr0LQSqG1QxqTX93vxtusdAJyhjM3O5PZCieu4i
unL3i9qyEyFS2zbHbPFSB48u82UyMJ8GLNIwntJoSWfWPssoJMz9zK6KpZhmbTWJ
-----END RSA PRIVATE KEY-----
""".trimIndent()

        private val TEST_CERT_PEM = """
-----BEGIN CERTIFICATE-----
MIIDFzCCAf+gAwIBAgIUWCzZItVIlSyv2q1SAPkmK8aNkv0wDQYJKoZIhvcNAQEL
BQAwGzEZMBcGA1UEAwwQQ0JMLVRlc3QtS2V5UGFpcjAeFw0yNjA3MTEwNDI0MDla
Fw0zNjA3MDgwNDI0MDlaMBsxGTAXBgNVBAMMEENCTC1UZXN0LUtleVBhaXIwggEi
MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZC2tlJwOPYP2alRVzXnm9slkb
vrG2/JKGiw4CEPNyiqmL3oKMNjJto4OvFK7Lqy06CQUGviAR8Vg/WpA/8OBbk01f
ITgb+hzjbBj29T3ZtvXAPuXZlDiJCw7ImsryqnM1L/1HZb3DKTe8JK2cNbEBndn/
0ppXsxjU4X+rKT2xoVLrsdWCPQ/DWsdO3hhb5s81xvXpGP+iF35e6PLEU55HacCO
IA3k6K3jhD2Ze2gziEdr2jSJl99wpSHmqleWhtQNW9o6EzHLY5yGpJ3M2S6jFBUA
iZIAijfz2QrNiTDD8CIpuaf2voht5cm21UWRtJ3hv36MnkGqqz5XB5dXzCYzAgMB
AAGjUzBRMB0GA1UdDgQWBBRDidW0eDQ1y7eVBw4dJYOd6de/4zAfBgNVHSMEGDAW
gBRDidW0eDQ1y7eVBw4dJYOd6de/4zAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3
DQEBCwUAA4IBAQCAlD+NMgXna22YpY/uRxZlN6mSIHrqB2K+hcpIrY6fJouzv6p0
PCIqg2T4jnjM/MUWggCZHlB+0eMwetSJw26JUIl4t69Z94jmRfLOv7dEKQyv1Run
ooHaJIrbYvaaE0yd0sOdGzbpZOlNBoy0lH3pqn008UIsIbgffss/bAVSfVE0s1g1
/7a6tHjh8zTdhNBHj8ihQOsr6Q0lUi/y6UUOrChn1lf3RhtodNTQvBUp2s6DpEE6
BvMa7VyUb8AmzyYLMqqsw2N57iGGgpKL8lbBGn27sLV0Q+pBUy9hNlI3XRs2fScO
grERjiGpyOdIWUZCTXp71+IUyu6/F40F5Rwr
-----END CERTIFICATE-----
""".trimIndent()
    }
}

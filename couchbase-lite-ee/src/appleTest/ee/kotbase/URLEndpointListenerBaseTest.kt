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
@file:Suppress("DEPRECATION")

package kotbase

import cocoapods.CouchbaseLite.kCBLCertAttrCommonName
import kotbase.ext.toSecCertificate
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFStringRefVar
import platform.Foundation.NSUserDefaults
import platform.Security.SecCertificateCopyCommonName
import platform.Security.errSecSuccess
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@OptIn(ExperimentalNativeApi::class)
open class URLEndpointListenerBaseTest : BaseReplicatorTest() {

    val wsPort = 4984
    val wssPort = 4985
    val serverCertLabel = "CBL-Server-Cert"
    val clientCertLabel = "CBL-Client-Cert"

    var listener: URLEndpointListener? = null

    val isHostApp: Boolean
        get() {
            return if (Platform.osFamily == OsFamily.IOS) {
                val defaults = NSUserDefaults.standardUserDefaults
                defaults.boolForKey("hostApp")
            } else {
                true
            }
        }

    val keyChainAccessAllowed: Boolean
        get() {
            return if (Platform.osFamily == OsFamily.IOS) {
                isHostApp
            } else {
                true
            }
        }

    // Helper methods
    // Listener Helper methods

    protected fun startListener(
        tls: Boolean = true,
        auth: ListenerAuthenticator? = null
    ): URLEndpointListener {
        // Stop:
        listener?.stop()

        // Listener:
        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.port = if (tls) wssPort else wsPort
        config.isTlsDisabled = !tls
        config.authenticator = auth

        return startListener(config)
    }

    protected fun startListener(config: URLEndpointListenerConfiguration): URLEndpointListener {
        listener = URLEndpointListener(config)

        // Start:
        listener!!.start()

        return listener!!
    }

    protected fun stopListener(listener: URLEndpointListener? = null) {
        val l = listener ?: this.listener

        l?.stop()
        // TODO: private API
        //l?.tlsIdentity?.deleteFromKeyChain()
    }

    private fun cleanUpIdentities() {
        try {
            // TODO: private API
            //URLEndpointListener.deleteAnonymousIdentities()
        } catch (e: Exception) {
            //ignore
        }
    }

    // TLS Identity helpers

    fun createTLSIdentity(isServer: Boolean = true): TLSIdentity? {
        if (!keyChainAccessAllowed) return null

        val label = if (isServer) serverCertLabel else clientCertLabel

        // cleanup client cert authenticator identity
        TLSIdentity.deleteIdentity(label)

        // Create client identity:
        val attrs = mapOf(kCBLCertAttrCommonName to if (isServer) "CBL-Server" else "daniel")
        return TLSIdentity.createIdentity(false, attrs, null, label)
    }

    protected fun checkCertificateEqual(cert1: ByteArray, cert2: ByteArray) {
        memScoped {
            val cn1 = alloc<CFStringRefVar>()
            assertEquals(errSecSuccess, SecCertificateCopyCommonName(cert1.toSecCertificate(), cn1.ptr))

            val cn2 = alloc<CFStringRefVar>()
            assertEquals(errSecSuccess, SecCertificateCopyCommonName(cert2.toSecCertificate(), cn2.ptr))

            assertEquals(cn1.value!!, cn2.value!!)
        }
    }

    // Replicator helper methods

    /// - Note: default value for continuous is true! That is common in this test suite
    protected fun createReplicator(
        db: Database,
        target: Endpoint,
        continuous: Boolean = true,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        serverCert: ByteArray? = null
    ): Replicator {
        val config = ReplicatorConfiguration(db, target)
        config.type = type
        config.isContinuous = continuous
        config.pinnedServerCertificate = serverCert
        return Replicator(config)
    }

    protected fun run(
        target: Endpoint,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        continuous: Boolean = false,
        auth: Authenticator? = null,
        serverCert: ByteArray? = null,
        expectedError: Int = 0
    ) {
        val config = makeConfig(target, type, continuous).apply {
            authenticator = auth
            pinnedServerCertificate = serverCert
        }
        config.run(errCode = expectedError)
    }

    protected fun run(
        target: Endpoint,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        continuous: Boolean = false,
        auth: Authenticator? = null,
        acceptSelfSignedOnly: Boolean = false,
        serverCert: ByteArray? = null,
        maxAttempts: Int = 0,
        expectedError: Int = 0
    ) {
        val config = makeConfig(
            target,
            type,
            continuous,
            auth,
            acceptSelfSignedOnly,
            serverCert,
            maxAttempts
        )
        config.run(errCode = expectedError)
    }

    protected fun makeConfig(
        target: Endpoint,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        continuous: Boolean = false,
        auth: Authenticator? = null,
        acceptSelfSignedOnly: Boolean = false,
        serverCert: ByteArray? = null,
        maxAttempts: Int = 0
    ): ReplicatorConfiguration {
        return makeConfig(target, type, continuous).apply {
            authenticator = auth
            pinnedServerCertificate = serverCert
            this.maxAttempts = maxAttempts
            isAcceptOnlySelfSignedServerCertificate = acceptSelfSignedOnly
        }
    }

    @BeforeTest
    fun setUp() {
        cleanUpIdentities()
    }

    @AfterTest
    fun tearDown() {
        stopListener()
        cleanUpIdentities()
    }
}

package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.kCBLCertAttrCommonName
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFStringRefVar
import platform.Foundation.NSUserDefaults
import platform.Security.SecCertificateCopyCommonName
import platform.Security.SecCertificateRef
import platform.Security.errSecSuccess
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

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
        val config = URLEndpointListenerConfiguration(otherDB)
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

    protected fun checkCertificateEqual(cert1: SecCertificateRef, cert2: SecCertificateRef) {
        memScoped {
            val cn1 = alloc<CFStringRefVar>()
            assertEquals(errSecSuccess, SecCertificateCopyCommonName(cert1, cn1.ptr))

            val cn2 = alloc<CFStringRefVar>()
            assertEquals(errSecSuccess, SecCertificateCopyCommonName(cert2, cn2.ptr))

            assertEquals(cn1.value!!, cn2.value!!)
        }
    }

    // Replicator helper methods

    /// - Note: default value for continuous is true! Thats is common in this test suite
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
        run(config, expectedError)
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
        run(config, expectedError)
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

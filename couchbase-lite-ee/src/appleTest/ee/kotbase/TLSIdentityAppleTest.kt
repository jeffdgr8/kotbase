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

import kotbase.internal.utils.PlatformUtils
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.io.readByteArray
import platform.CoreFoundation.CFArrayGetCount
import platform.CoreFoundation.CFArrayGetValueAtIndex
import platform.CoreFoundation.CFArrayRef
import platform.CoreFoundation.CFArrayRefVar
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecCertificateRef
import platform.Security.SecIdentityCopyPrivateKey
import platform.Security.SecIdentityRef
import platform.Security.SecItemDelete
import platform.Security.SecKeyRefVar
import platform.Security.SecPKCS12Import
import platform.Security.errSecInvalidItemRef
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecClass
import platform.Security.kSecClassCertificate
import platform.Security.kSecClassKey
import platform.Security.kSecImportExportPassphrase
import platform.Security.kSecImportItemCertChain
import platform.Security.kSecImportItemIdentity
import platform.Security.kSecValueRef
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.*

// TLSIdentityTest.m
// Apple-only TLSIdentity tests, using the platform-specific
// TLSIdentity.importIdentity() and createIdentity(SecIdentityRef) APIs

@OptIn(ExperimentalNativeApi::class)
class TLSIdentityAppleTest : BaseTest() {

    @BeforeTest
    fun setUpTLSIdentityAppleTest() {
        if (!keyStoreAccessAllowed) return
        TLSIdentity.deleteIdentity(SERVER_CERT_LABEL)
    }

    @AfterTest
    fun tearDownTLSIdentityAppleTest() {
        if (!keyStoreAccessAllowed) return
        TLSIdentity.deleteIdentity(SERVER_CERT_LABEL)
    }

    private fun deleteFromKeyChain(secClass: CFStringRef?, item: CFTypeRef) {
        val query = CFDictionaryCreateMutable(null, 2, null, null)
        CFDictionaryAddValue(query, kSecClass, secClass)
        CFDictionaryAddValue(query, kSecValueRef, item)
        val status = SecItemDelete(query)
        CFRelease(query)
        assertTrue(
            status == errSecSuccess || status == errSecItemNotFound || status == errSecInvalidItemRef,
            "SecItemDelete returned $status"
        )
    }

    @Test
    fun testImportIdentity() {
        // TODO: CBL-7005: importIdentityWithData not working on newer macOS
        if (Platform.osFamily == OsFamily.MACOSX) return

        if (!keyStoreAccessAllowed) return

        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.readByteArray()
        }

        var identity = TLSIdentity.importIdentity(data, "123".toCharArray(), SERVER_CERT_LABEL)
        assertEquals(2, identity.certs.size)

        // Get:
        identity = assertNonNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))
        assertEquals(2, identity.certs.size)

        // Delete:
        TLSIdentity.deleteIdentity(SERVER_CERT_LABEL)

        // Get:
        assertNull(TLSIdentity.getIdentity(SERVER_CERT_LABEL))
    }

    // TODO: crashes the test process on macOS in the
    //  SecPKCS12Import/identityWithIdentity Keychain access
    //  (CBL-7005: PKCS12 import is broken on newer macOS)
    @Ignore
    @Test
    fun testGetIdentityWithIdentity() {
        // SecPKCS12Import stores the imported identity in the Keychain on macOS only.
        // (The ObjC test stores the private key and certificates manually on iOS.)
        if (Platform.osFamily != OsFamily.MACOSX) return

        if (!keyStoreAccessAllowed) return

        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.readByteArray()
        }

        memScoped {
            // Use SecPKCS12Import to import the PKCS12 data:
            val cfData = data.usePinned { pinned ->
                CFDataCreate(null, pinned.addressOf(0).reinterpret(), data.size.toLong())
            }
            val password = CFStringCreateWithCString(null, "123", kCFStringEncodingUTF8)
            val options = CFDictionaryCreateMutable(null, 1, null, null)
            CFDictionaryAddValue(options, kSecImportExportPassphrase, password)

            val importedItems = alloc<CFArrayRefVar>()
            val status = SecPKCS12Import(cfData, options, importedItems.ptr)
            assertEquals(errSecSuccess, status)

            val items = importedItems.value!!
            assertTrue(CFArrayGetCount(items) > 0)
            val item: CFDictionaryRef = CFArrayGetValueAtIndex(items, 0)!!.reinterpret()

            // Identity:
            val identityRef: SecIdentityRef =
                assertNotNull(CFDictionaryGetValue(item, kSecImportItemIdentity)).reinterpret()

            // Private Key:
            val privateKey = alloc<SecKeyRefVar>()
            assertEquals(errSecSuccess, SecIdentityCopyPrivateKey(identityRef, privateKey.ptr))

            // Certs:
            val certChain: CFArrayRef =
                assertNotNull(CFDictionaryGetValue(item, kSecImportItemCertChain)).reinterpret()
            assertEquals(2, CFArrayGetCount(certChain))
            val rootCert: SecCertificateRef = CFArrayGetValueAtIndex(certChain, 1)!!.reinterpret()

            // Get identity:
            val identity = TLSIdentity.createIdentity(identityRef, listOf(rootCert))
            assertEquals(2, identity.certs.size)

            // Cleanup: remove the certificates and private key from the Keychain:
            deleteFromKeyChain(kSecClassKey, privateKey.value!!)
            for (i in 0 until CFArrayGetCount(certChain)) {
                deleteFromKeyChain(
                    kSecClassCertificate,
                    CFArrayGetValueAtIndex(certChain, i)!!.reinterpret()
                )
            }

            CFRelease(privateKey.value)
            CFRelease(items)
            CFRelease(options)
            CFRelease(password)
            CFRelease(cfData)
        }
    }

    companion object {
        private const val SERVER_CERT_LABEL = "CBL-Server-Cert"
    }
}

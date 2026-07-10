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
import kotlinx.io.readByteArray
import java.io.ByteArrayInputStream
import java.security.KeyStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// TLSIdentityTest.m
// JVM equivalent of the apple testImportIdentity test: import the
// PKCS12 data into a KeyStore and get the identity backed by its entry

class TLSIdentityJvmTest : BaseTest() {

    @Test
    fun testGetIdentityFromImportedKeyStoreEntry() {
        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.readByteArray()
        }

        val password = "123".toCharArray()
        val keyStore = KeyStore.getInstance("PKCS12")
        ByteArrayInputStream(data).use { input ->
            keyStore.load(input, password)
        }
        val alias = keyStore.aliases().nextElement()

        val identity = TLSIdentity.getIdentity(keyStore, alias, password)
        assertNotNull(identity)
        assertEquals(2, identity.certs.size)
    }
}

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

import kotbase.internal.fleece.toFLString
import kotlinx.cinterop.*
import libcblite.CBLEncryptionKey
import libcblite.CBLEncryptionKey_FromPassword
import libcblite.kCBLEncryptionAES256
import libcblite.kCBLEncryptionKeySizeAES256
import platform.posix.memcpy
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class EncryptionKey
internal constructor(actual: CPointer<CBLEncryptionKey>? = null) {

    private val arena = Arena()

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(arena) {
        it.clear()
    }

    internal val actual: CPointer<CBLEncryptionKey> =
        actual ?: arena.alloc<CBLEncryptionKey>().apply {
            algorithm = kCBLEncryptionAES256
        }.ptr

    public actual constructor(key: ByteArray) : this() {
        if (key.size.toUInt() != kCBLEncryptionKeySizeAES256) {
            throw IllegalArgumentException("Key size is invalid. Key must be a 256-bit (32-byte) key.")
        }
        memcpy(actual.pointed.bytes, key.refTo(0), kCBLEncryptionKeySizeAES256.convert())
    }

    public actual constructor(password: String) : this() {
        memScoped {
            if (!CBLEncryptionKey_FromPassword(actual, password.toFLString(this))) {
                throw IllegalArgumentException("Error deriving key from password")
            }
        }
    }
}

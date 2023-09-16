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

import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import libcblite.kCBLEncryptionKeySizeAES256
import libcblite.kCBLEncryptionNone
import platform.posix.memcpy

public actual var DatabaseConfiguration.encryptionKey: EncryptionKey?
    get() {
        val ec = actual.pointed.encryptionKey
        return if (ec.algorithm != kCBLEncryptionNone) {
            EncryptionKey(ec.ptr)
        } else null
    }
    set(value) {
        val ec = value?.actual?.pointed
        with(actual.pointed.encryptionKey) {
            if (ec != null) {
                algorithm = ec.algorithm
                memcpy(bytes, ec.bytes, kCBLEncryptionKeySizeAES256.convert())
            } else {
                algorithm = kCBLEncryptionNone
            }
        }
    }

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
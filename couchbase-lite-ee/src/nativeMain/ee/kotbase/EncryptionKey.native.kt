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
        if (!CBLEncryptionKey_FromPassword(actual, password.toFLString())) {
            throw IllegalArgumentException("Error deriving key from password")
        }
    }
}

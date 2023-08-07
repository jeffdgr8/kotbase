package kotbase.ext

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.data
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateCreateWithData
import platform.Security.SecCertificateRef
import platform.posix.memcpy

public fun ByteArray.toNSData(): NSData {
    return if (isNotEmpty()) {
        usePinned {
            NSData.create(bytes = it.addressOf(0), length = size.convert())
        }
    } else {
        NSData.data()
    }
}

public fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            memcpy(refTo(0), bytes, length)
        }
    }
}

public fun ByteArray.toCFData(): CFDataRef {
    return CFDataCreate(
        null,
        asUByteArray().refTo(0),
        size.convert()
    )!!
}

public fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this)
    return UByteArray(length.toInt()).apply {
        val range = CFRangeMake(0, length)
        CFDataGetBytes(this@toByteArray, range, refTo(0))
    }.asByteArray()
}

public fun ByteArray.toSecCertificate(): SecCertificateRef =
    SecCertificateCreateWithData(kCFAllocatorDefault, toCFData())!!

public fun SecCertificateRef.toByteArray(): ByteArray =
    SecCertificateCopyData(this)!!.toByteArray()

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

internal fun ByteArray.toNSData(): NSData {
    return if (isNotEmpty()) {
        usePinned {
            NSData.create(bytes = it.addressOf(0), length = size.convert())
        }
    } else {
        NSData.data()
    }
}

internal fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            memcpy(refTo(0), bytes, length)
        }
    }
}

internal fun ByteArray.toCFData(): CFDataRef {
    return CFDataCreate(
        null,
        asUByteArray().refTo(0),
        size.convert()
    )!!
}

internal fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this)
    return UByteArray(length.toInt()).apply {
        val range = CFRangeMake(0, length)
        CFDataGetBytes(this@toByteArray, range, refTo(0))
    }.asByteArray()
}

internal fun ByteArray.toSecCertificate(): SecCertificateRef =
    SecCertificateCreateWithData(kCFAllocatorDefault, toCFData())!!

internal fun SecCertificateRef.toByteArray(): ByteArray =
    SecCertificateCopyData(this)!!.toByteArray()

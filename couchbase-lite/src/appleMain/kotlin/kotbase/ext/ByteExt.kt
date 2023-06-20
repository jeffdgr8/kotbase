package kotbase.ext

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.CoreFoundation.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateCreateWithData
import platform.Security.SecCertificateRef
import platform.posix.memcpy

internal fun ByteArray.toNSData(): NSData {
    return memScoped {
        NSData.create(bytes = allocArrayOf(this@toNSData), length = size.convert())
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

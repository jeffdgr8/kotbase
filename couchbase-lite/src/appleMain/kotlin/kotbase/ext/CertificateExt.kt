package kotbase.ext

import platform.CoreFoundation.kCFAllocatorDefault
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateCreateWithData
import platform.Security.SecCertificateRef

public fun ByteArray.toSecCertificate(): SecCertificateRef =
    SecCertificateCreateWithData(kCFAllocatorDefault, toCFData()) ?: error("Invalid SecCertificateRef data")

public fun SecCertificateRef.toByteArray(): ByteArray =
    SecCertificateCopyData(this)!!.toByteArray()

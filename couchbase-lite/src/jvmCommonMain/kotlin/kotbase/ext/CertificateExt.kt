package kotbase.ext

import java.io.ByteArrayInputStream
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

internal expect fun ByteArray.toBase64String(): String

public fun ByteArray.toCertificate(
    certFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
): Certificate {
    val base64 = "-----BEGIN CERTIFICATE-----\n${toBase64String()}\n-----END CERTIFICATE-----"
    val inputStream = ByteArrayInputStream(base64.encodeToByteArray())
    return certFactory.generateCertificate(inputStream)
}

public fun List<ByteArray>.toCertificates(
    certFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
): List<Certificate> =
    map { it.toCertificate(certFactory) }

public fun Certificate.toByteArray(): ByteArray = encoded

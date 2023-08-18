package kotbase.ext

import okio.ByteString.Companion.decodeBase64
import kotlin.test.Test
import kotlin.test.assertContentEquals

class CertificateExtTest {

    @Test
    fun testCertificate() {
        val certString = """
            MIICSjCCAbOgAwIBAgIBADANBgkqhkiG9w0BAQ0FADBCMQswCQYDVQQGEwJ1czEL
            MAkGA1UECAwCVVQxEDAOBgNVBAoMB0tvdGJhc2UxFDASBgNVBAMMC2tvdGJhc2Uu
            ZGV2MB4XDTIzMDgxNjIzMTkyN1oXDTI0MDgxNTIzMTkyN1owQjELMAkGA1UEBhMC
            dXMxCzAJBgNVBAgMAlVUMRAwDgYDVQQKDAdLb3RiYXNlMRQwEgYDVQQDDAtrb3Ri
            YXNlLmRldjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAxCiY4y6de8zDoNIZ
            hjF58cITrf6cFGfr1mfkx0i9Q5hSP5lWN/OqF6cdlEpske23YpH907yezpiDsJdn
            80d+erpLQTFgevGvRnjXsPuvj7ZAiiwkwomMOJ0uzAnWLHSR2zaOntKnI73nBNjh
            FrsxPKSIdYJBlS9BZGp6eGkagLkCAwEAAaNQME4wHQYDVR0OBBYEFIySx/XnaGSE
            o08+3AW1ZqkplAeRMB8GA1UdIwQYMBaAFIySx/XnaGSEo08+3AW1ZqkplAeRMAwG
            A1UdEwQFMAMBAf8wDQYJKoZIhvcNAQENBQADgYEAbZQpuiDaOp77IQFghAoRNT5a
            ionEqW0+g7bRspgLbmykQ1vmHoW7fVZEcFlUADTygAIsIPgpmftuc60Nbe+VbfpK
            VGnrtSOmLChgcn6r5OKmHbq8yzcWwlO20+8UZaDXAXwx1/y6DZ2yx8BinAGDNo65
            q0+V3Wv6fCIURn61Vpw=
            """.trimIndent().replace("\n", "")
        val certBinary = certString.decodeBase64()!!.toByteArray()

        val cert = certBinary.toCertificate()
        val byteArray = cert.toByteArray()
        assertContentEquals(certBinary, byteArray)
    }
}

package kotbase.ext

import java.util.Base64

internal actual fun ByteArray.toBase64String(): String =
    Base64.getEncoder().encodeToString(this)

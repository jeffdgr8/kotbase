package kotbase.ext

import android.util.Base64

internal actual fun ByteArray.toBase64String(): String =
    Base64.encodeToString(this, Base64.NO_WRAP)

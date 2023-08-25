package okio.ext

import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import platform.Foundation.NSUnderlyingErrorKey

internal fun Exception.toNSError() = NSError(
  domain = "Kotlin",
  code = 0,
  userInfo = mapOf(
    NSLocalizedDescriptionKey to message,
    NSUnderlyingErrorKey to this,
  ),
)

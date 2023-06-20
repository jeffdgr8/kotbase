package kotbase.ext

import platform.Foundation.NSNumber

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun NSNumber.asNumber(): Number = when (val any = this as Any) {
    // NSNumber can be kotlin.Boolean if created as a boolean
    is Boolean -> if (any) 1 else 0
    else -> this as Number
}

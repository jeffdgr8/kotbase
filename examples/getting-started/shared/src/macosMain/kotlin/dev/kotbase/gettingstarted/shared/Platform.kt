package dev.kotbase.gettingstarted.shared

import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

actual class Platform actual constructor() {
    actual val platform: String = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        "macOS $majorVersion.$minorVersion"
    }
}

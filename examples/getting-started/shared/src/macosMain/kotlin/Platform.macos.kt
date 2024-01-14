import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

class MacOSPlatform : Platform {
    override val name: String = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        "macOS $majorVersion.$minorVersion"
    }
}

actual fun getPlatform(): Platform = MacOSPlatform()

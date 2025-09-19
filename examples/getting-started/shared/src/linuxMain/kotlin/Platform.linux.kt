import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.uname
import platform.posix.utsname

class LinuxPlatform : Platform {
    override val name: String = memScoped {
        with(alloc<utsname>()) {
            uname(ptr)
            "${sysname.toKString()} ${release.toKString()}"
        }
    }
}

actual fun getPlatform(): Platform = LinuxPlatform()

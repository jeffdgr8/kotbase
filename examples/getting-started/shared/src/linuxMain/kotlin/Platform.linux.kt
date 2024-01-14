import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.uname
import platform.posix.utsname

class LinuxPlatform : Platform {
    override val name: String = memScoped {
        val utsname = alloc<utsname>()
        uname(utsname.ptr)
        utsname.sysname.toKString() + " " + utsname.release.toKString()
    }
}

actual fun getPlatform(): Platform = LinuxPlatform()

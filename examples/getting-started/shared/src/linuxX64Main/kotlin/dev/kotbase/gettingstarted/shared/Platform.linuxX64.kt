package dev.kotbase.gettingstarted.shared

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.uname
import platform.posix.utsname

actual class Platform actual constructor() {
    actual val platform: String = memScoped {
        val utsname = alloc<utsname>()
        uname(utsname.ptr)
        utsname.sysname.toKString() + " " + utsname.release.toKString()
    }
}

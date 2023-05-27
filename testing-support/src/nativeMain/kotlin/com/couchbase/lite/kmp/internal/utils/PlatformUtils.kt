package com.couchbase.lite.kmp.internal.utils

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source
import kotlin.native.internal.GC

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        GC.collect()
    }

    override fun getAsset(asset: String): Source {
        val target = when (Platform.osFamily) {
            OsFamily.LINUX -> "linux"
            OsFamily.WINDOWS -> "mingw"
            else -> error("Unsupported platform: ${Platform.osFamily}")
        } + Platform.cpuArchitecture.name.lowercase().replaceFirstChar(Char::titlecase)
        val path = "build/bin/$target/debugTest/$asset".toPath()
        return FileSystem.SYSTEM.source(path)
    }
}

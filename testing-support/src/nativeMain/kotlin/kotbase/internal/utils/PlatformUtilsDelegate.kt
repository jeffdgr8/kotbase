package kotbase.internal.utils

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    @OptIn(NativeRuntimeApi::class)
    override fun gc() {
        GC.collect()
    }

    @OptIn(ExperimentalNativeApi::class)
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

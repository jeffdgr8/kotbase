package kotbase.internal.utils

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
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
        val path = Path("build/bin/$target/debugTest/$asset")
        return SystemFileSystem.source(path).buffered()
    }
}

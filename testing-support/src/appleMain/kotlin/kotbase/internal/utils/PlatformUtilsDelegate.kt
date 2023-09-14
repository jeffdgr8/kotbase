package kotbase.internal.utils

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSBundle
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    @OptIn(NativeRuntimeApi::class)
    override fun gc() {
        GC.collect()
    }

    override fun getAsset(asset: String): Source? {
        val dotIndex = asset.lastIndexOf('.')
        val name = asset.substring(0, dotIndex)
        val type = asset.substring(dotIndex + 1)
        val path = NSBundle.mainBundle
            .pathForResource(name, type)
            ?: return null
        return SystemFileSystem.source(Path(path)).buffered()
    }
}

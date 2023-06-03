package kotbase.internal.utils

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source
import platform.Foundation.NSBundle
import kotlin.native.internal.GC

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

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
        return FileSystem.SYSTEM.source(path.toPath())
    }
}

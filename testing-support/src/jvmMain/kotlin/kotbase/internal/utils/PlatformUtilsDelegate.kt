package kotbase.internal.utils

import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        System.gc()
    }

    override fun getAsset(asset: String): Source? =
        javaClass.getResource("/$asset")?.openStream()?.asSource()?.buffered()
}

package kotbase.internal.utils

import okio.Source

object PlatformUtils {

    private val DELEGATE: Delegate = PlatformUtilsDelegate()

    fun gc() {
        DELEGATE.gc()
    }

    fun getAsset(asset: String): Source? = DELEGATE.getAsset(asset)

    interface Delegate {
        fun gc()
        fun getAsset(asset: String): Source?
    }
}
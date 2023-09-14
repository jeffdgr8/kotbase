package kotbase.internal.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        System.gc()
    }

    override fun getAsset(asset: String): Source =
        getApplicationContext<Context>().assets.open(asset).asSource().buffered()
}

package kotbase.internal.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import okio.Source
import okio.source

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        System.gc()
    }

    override fun getAsset(asset: String): Source =
        getApplicationContext<Context>().assets.open(asset).source()
}

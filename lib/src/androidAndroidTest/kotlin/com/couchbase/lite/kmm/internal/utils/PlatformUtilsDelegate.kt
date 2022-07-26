package com.couchbase.lite.kmm.internal.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import okio.IOException
import okio.Source
import okio.source

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        System.gc()
    }

    override fun getAsset(asset: String?): Source? {
        if (asset == null) {
            return null
        }
        return try {
            getApplicationContext<Context>().assets.open(asset).source()
        } catch (e: IOException) {
            null
        }
    }
}

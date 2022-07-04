package com.couchbase.lite.kmm.internal.utils

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import okio.IOException
import okio.Source
import okio.source
import java.lang.IllegalArgumentException
import java.util.*

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

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

    override val encoder: PlatformUtils.Base64Encoder
        get() {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                PlatformUtils.Base64Encoder { src ->
                    android.util.Base64.encodeToString(src, android.util.Base64.DEFAULT)
                }
            } else {
                PlatformUtils.Base64Encoder { src ->
                    if (src == null) null else Base64.getEncoder().encodeToString(src)
                }
            }
        }

    override val decoder: PlatformUtils.Base64Decoder
        get() {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                PlatformUtils.Base64Decoder { src ->
                    try {
                        android.util.Base64.decode(src, android.util.Base64.DEFAULT)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            } else {
                PlatformUtils.Base64Decoder { src ->
                    try {
                        if (src == null) null else Base64.getDecoder().decode(src)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            }
        }
}

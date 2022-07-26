package com.couchbase.lite.kmm.internal.utils

import com.udobny.kmm.ext.toByteArray
import com.udobny.kmm.ext.toNSData
import okio.Source
import okio.source
import platform.Foundation.*
import kotlin.native.internal.GC

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        GC.collect()
    }

    override fun getAsset(asset: String?): Source? {
        asset ?: return null
        val dotIndex = asset.lastIndexOf('.')
        val filePath = asset.substring(0, dotIndex)
        val ext = asset.substring(dotIndex + 1)
        val path = NSBundle.mainBundle
            .pathForResource("resources/$filePath", ext)
            ?: return null
        return NSInputStream(NSURL(fileURLWithPath = path)).source()
    }

    override val encoder: PlatformUtils.Base64Encoder
        get() = PlatformUtils.Base64Encoder { src ->
            src?.toNSData()?.base64EncodedStringWithOptions(0)
        }

    override val decoder: PlatformUtils.Base64Decoder
        get() = PlatformUtils.Base64Decoder { src ->
            (src as NSString?)?.dataUsingEncoding(NSUTF8StringEncoding)?.toByteArray()
        }
}

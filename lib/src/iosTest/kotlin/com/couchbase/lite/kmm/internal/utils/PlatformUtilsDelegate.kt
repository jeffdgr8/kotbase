package com.couchbase.lite.kmm.internal.utils

import com.udobny.kmm.ext.toByteArray
import com.udobny.kmm.ext.toNSData
import okio.Buffer
import okio.Source
import platform.Foundation.*

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        // TODO: Is this possible to request with Kotlin/Native?
    }

    override fun getAsset(asset: String?): Source? {
        if (asset == null) {
            return null
        }
        val dotIndex = asset.lastIndexOf('.')
        val filePath = asset.substring(0, dotIndex)
        val ext = asset.substring(dotIndex + 1)
        val path = NSBundle.mainBundle
            .pathForResource("resources/$filePath", ext)
            ?: return null
        // TODO: stream when https://github.com/square/okio/pull/1123 is available
        //return NSInputStream(NSURL(path))?.source()
        val data = NSData.dataWithContentsOfFile(path)
            ?.toByteArray()
            ?: return null
        return Buffer().apply {
            write(data)
        }
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

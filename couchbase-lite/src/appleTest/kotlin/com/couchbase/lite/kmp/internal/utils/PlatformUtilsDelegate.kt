package com.couchbase.lite.kmp.internal.utils

import okio.Source
import okio.source
import platform.Foundation.*
import kotlin.native.internal.GC

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        GC.collect()
    }

    override fun getAsset(asset: String): Source? {
        val dotIndex = asset.lastIndexOf('.')
        val filePath = asset.substring(0, dotIndex)
        val ext = asset.substring(dotIndex + 1)
        val path = NSBundle.mainBundle
            .pathForResource(filePath, ext)
            ?: return null
        return NSInputStream(NSURL(fileURLWithPath = path)).source()
    }
}

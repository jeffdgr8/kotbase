package com.couchbase.lite.kmp.internal.utils

import okio.Source
import okio.source

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        System.gc()
    }

    override fun getAsset(asset: String): Source? =
        javaClass.getResource("/$asset")?.openStream()?.source()
}

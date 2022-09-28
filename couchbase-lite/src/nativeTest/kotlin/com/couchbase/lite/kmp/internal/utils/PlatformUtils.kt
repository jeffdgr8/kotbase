package com.couchbase.lite.kmp.internal.utils

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source
import kotlin.native.internal.GC

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    override fun gc() {
        GC.collect()
    }

    override fun getAsset(asset: String): Source {
        return FileSystem.SYSTEM.source(asset.toPath())
    }
}

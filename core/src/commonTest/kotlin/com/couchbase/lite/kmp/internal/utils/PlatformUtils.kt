package com.couchbase.lite.kmp.internal.utils

import okio.Source

object PlatformUtils {

    private val DELEGATE: Delegate = PlatformUtilsDelegate()

    fun gc() {
        DELEGATE.gc()
    }

    fun getAsset(asset: String): Source? {
        return DELEGATE.getAsset(asset)
    }

    internal interface Delegate {
        fun gc()
        fun getAsset(asset: String): Source?
    }
}

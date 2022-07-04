package com.couchbase.lite.kmm.internal.utils

import okio.Source

object PlatformUtils {

    private val DELEGATE: Delegate = PlatformUtilsDelegate()

    fun getAsset(asset: String?): Source? {
        return DELEGATE.getAsset(asset)
    }

    val encoder: Base64Encoder
        get() = DELEGATE.encoder

    val decoder: Base64Decoder
        get() = DELEGATE.decoder

    fun interface Base64Encoder {
        fun encodeToString(src: ByteArray?): String?
    }

    fun interface Base64Decoder {
        fun decodeString(src: String?): ByteArray?
    }

    internal interface Delegate {
        fun getAsset(asset: String?): Source?
        val encoder: Base64Encoder
        val decoder: Base64Decoder
    }
}

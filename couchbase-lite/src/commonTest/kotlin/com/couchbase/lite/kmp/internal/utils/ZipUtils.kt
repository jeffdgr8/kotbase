package com.couchbase.lite.kmp.internal.utils

import okio.Source

expect object ZipUtils {

    fun unzip(input: Source, destination: String)
}

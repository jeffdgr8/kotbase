@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp.internal.utils

import okio.Source

expect object ZipUtils {

    fun unzip(input: Source, destination: String)
}

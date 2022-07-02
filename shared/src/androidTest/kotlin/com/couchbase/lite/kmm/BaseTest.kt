package com.couchbase.lite.kmm

import java.io.File

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual fun dirExists(dir: String): Boolean {
    return File(dir).exists()
}

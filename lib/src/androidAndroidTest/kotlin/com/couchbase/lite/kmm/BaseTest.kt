package com.couchbase.lite.kmm

import java.io.File

actual fun dirExists(dir: String): Boolean {
    return File(dir).exists()
}

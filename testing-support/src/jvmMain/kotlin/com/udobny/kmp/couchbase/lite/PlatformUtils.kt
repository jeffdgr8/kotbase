package com.udobny.kmp.couchbase.lite

import com.couchbase.lite.kmp.CouchbaseLite
import java.io.File

actual fun initCouchbaseLite() {
    val rootDir = File("build/cb-tmp")
    CouchbaseLite.init(true, rootDir, rootDir)
}
